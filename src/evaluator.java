import com.sun.xml.internal.bind.v2.TODO;
import org.omg.CORBA.Environment;

/**
 * Created by appleowner on 4/23/16.
 */
public class evaluator {
    Lexeme e;
    environment global;

    protected evaluator(Lexeme tree) {
        environment e = new environment();
        this.e = e.create();
        global = e; // idk about this ...
        runEvaluator(tree, this.e);
    }

    protected void runEvaluator(Lexeme tree, Lexeme env) {
        eval(tree, env);
    }

    private Lexeme eval(Lexeme tree, Lexeme env) {
        String tree_type = tree.type;
        System.out.println("evaluating tree of type " + tree_type);
        //System.out.println("tree.left " + tree.left);
        switch (tree_type) {
            case "functionDef": // function definition
                evalFuncDef(tree, env);
                break;
            case "function_call": // function call
                return evalFunctionCall(tree, env);
            case "block": // block/body
                return evalBlock(tree, env);
            case "binoperation": // binary operation
                return evalBinaryOp(tree, env);
            case "unioperation": // unary operation
                return evalUniOperation(tree, env);
            case "assignment": // assignment
                return evalAssingment(tree, env);
            case "VARDEF": // variable definition
                return evalVariableDef(tree, env);
            case "statementList":
                return evalStatementList(tree, env);
            case "statement":
                return evalStatement(tree, env);
            case "return":
                return evalReturn(tree, env);
            case "PRINT":
                evalPrint(tree, env); // doesn't return lexeme
                break;
            case "INTEGER":
                return tree; // maybe this is right? i don't think there's any case where it would have a left or right
            case "DOUBLE":
                return tree;
            case "STRING":
                return tree;
            case "BOOLEAN":
                return tree;
            case "primary":
                return evalPrimary(tree, env);
            case "expression":
                return evalExpression(tree, env);
        }
        return null;
    }

    // evaluate a function definition
    private void evalFuncDef(Lexeme tree, Lexeme env) {
        // need to make a closure and have it point to the defining environment
        Lexeme closure = new Lexeme("closure");
        closure.left = env; // left points to the defining environment
        closure.right = tree; // should point to the functionDef parse tree

        // need to double check if this is the right tree to be attaching
        String fnName = tree.left.left.sValue; // get the function name

        global.insert(env, new Lexeme("function_name", fnName), closure); // add the closure to the environment
    }

    // evaluate a function call
    private Lexeme evalFunctionCall(Lexeme tree, Lexeme env) {
        Lexeme closure = global.get(tree.left.sValue, env); // get the function's name from parse tree
        Lexeme args = getArgList(tree); // get arg list from parse tree
        Lexeme params = getParamList(closure.right); // get arg list from closure, pass in the functionDef parse tree
        Lexeme body = getClosureBody(closure.right); // get closure body, pass in functionDef parse tree
        Lexeme senv = getStaticEnvironment(closure); // get the static environment (defining environment of fn?)
        Lexeme eval_args = eval(args, env); // need to evaluate the args...// TODO is this supposed to be evalArgs specifically?
        Lexeme extended_env = global.extend(params, eval_args, env);

        return eval(body, extended_env);
    }

    // get parameters from function call tree
    private Lexeme getArgList(Lexeme tree) {
        Lexeme current_lexeme = tree.left.left; // first part of param list
        if(current_lexeme.left != null) {
            Lexeme args = current_lexeme.left;
            current_lexeme = current_lexeme.right;
            while (current_lexeme.left != null) { // walk through list getting args
                args.left = current_lexeme.left;
                current_lexeme = current_lexeme.right;
            }
            return args;
        }
        return null;
    }

    private Lexeme getParamList(Lexeme tree) {
        Lexeme current_lexeme = tree.left.left.left; // first part of param list
        if(current_lexeme.left != null) {
            Lexeme params = current_lexeme.left.left;
            current_lexeme = current_lexeme.right;
            while (current_lexeme.left != null) { // walk through list getting args
                params.left = current_lexeme.left.left;
                current_lexeme = current_lexeme.right;
            }
            return params;
        }
        return null;
    }

    private Lexeme getClosureBody(Lexeme tree) {
        return tree.left.right.left; // return body from parse tree
    }

    private Lexeme getStaticEnvironment(Lexeme closure) {
        return closure.left;
    }

    // need to evaluate each statement in the block's list of statements
    private Lexeme evalBlock(Lexeme tree, Lexeme env) {
        Lexeme statement_list = tree.left.left; // ignore brackets
        return evalStatementList(statement_list, env); // evaluate the statement list and return result
    }

    // evaluate a statement
    private Lexeme evalStatement(Lexeme tree, Lexeme env) {
        return eval(tree.left, env); // evaluate the nitty gritty (subtree) of the statement
    }

    // evaluate each statement in a statement list
    private Lexeme evalStatementList(Lexeme tree, Lexeme env) {
        Lexeme statement_list = tree;
        Lexeme current_statement = tree.left;
        //System.out.println("current_statement type " + current_statement.type);
        Lexeme result = null;
        while (current_statement != null) {
            result = eval(current_statement, env);
            statement_list = statement_list.right; // next statement list
            current_statement = statement_list.left; // next statement
        }
        return result;
    }

    // evaluate an expression
    private Lexeme evalExpression(Lexeme tree, Lexeme env) {
        return eval(tree.left, env);
    }

    private Lexeme evalBinaryOp(Lexeme tree, Lexeme env) {
        String op = tree.left.right.sValue;
        System.out.println("op type " + op /*+ "equal? " + (op.contentEquals("+"))*/);
        if (op.contentEquals("+")) {
            System.out.println("evaluating plus");
            return evalPlus(tree, env);
        } else if (op.contentEquals("*")) {
            return evalMult(tree, env);
        } else if (op.contentEquals("/")) {
            return evalDivision(tree, env);
        } else if (op.contentEquals("%")) {
            return evalMod(tree, env);
        } else if (op.contentEquals("^")) {
            return evalExponentiation(tree, env);
        }
        return null;
    }

    // evaluate binary addition
    private Lexeme evalPlus(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        System.out.println("left type " + left.type + " of value " + left.iValue);
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        System.out.println("right type " + right.type + " of value " + right.iValue);
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", left.iValue + right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue + right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue + right.dValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue + right.dValue);
        }
    }

    // evaluate binary multiplication
    private Lexeme evalMult(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", left.iValue * right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue * right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue * right.dValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue * right.dValue);
        }
    }

    // evaluate division
    private Lexeme evalDivision(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.iValue / right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue / right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue / right.dValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue / right.dValue);
        }
    }

    private Lexeme evalPrimary(Lexeme tree, Lexeme env) {
        //System.out.println("tree.type " + tree.type);
        return eval(tree.left, env); // evaluate the primary's value
    }

    // evaluate an exponentiation operation
    private Lexeme evalExponentiation(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", Math.pow((double) left.iValue, (double) right.iValue));
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", Math.pow(left.dValue, (double) right.iValue));
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", Math.pow((double) left.iValue,  right.dValue));
        } else {
            return new Lexeme("DOUBLE", Math.pow(left.dValue, right.dValue));
        }
    }

    // evaluate a mod operation
    private Lexeme evalMod(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.iValue % right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue % right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue % right.dValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue % right.dValue);
        }
    }

    // evaluate a unary operation
    private Lexeme evalUniOperation(Lexeme tree, Lexeme env) {
        String op = tree.left.right.sValue; // get the operator as a string
        if (op == "++") {
            return evalIncrement(tree, env);
        } else if (op == "--") {
            return evalDecrement(tree, env);
        }
        return null; // idk about this...
    }

    // evaluate an incrementation
    private Lexeme evalIncrement(Lexeme tree, Lexeme env) {
        Lexeme operand = eval(tree.left, env); // evaluate the operand
        if (operand.type == "INTEGER") {
            return new Lexeme("INTEGER", operand.iValue++);
        } else { // has to be a double TODO think about throwing error here
            return new Lexeme("DOUBLE", operand.dValue++);
        }
    }

    // evaluate a decremental operation
    private Lexeme evalDecrement(Lexeme tree, Lexeme env) {
        Lexeme operand = eval(tree.left, env); // evaluate the operand
        if (operand.type == "INTEGER") {
            return new Lexeme("INTEGER", operand.iValue--);
        } else {
            return new Lexeme("DOUBLE", operand.dValue--);
        }
    }

    // evaluate a return statement
    private Lexeme evalReturn(Lexeme tree, Lexeme env) {
        return eval(tree.left, env); // evaluate the return argument (primary)
    }

    // evaluate a print call
    private void evalPrint(Lexeme tree, Lexeme env) {
        System.out.println("lexeme tree type " + tree.type);
        System.out.println("lexeme tree type " + tree.left.type);
        System.out.println("lexeme tree type " + tree.left.left.type);
        Lexeme result = eval(tree.left.left, env); // get the print argument and evaluate it
        if (result.type == "STRING") {
            System.out.println(result.sValue);
        } else if (result.type == "BOOLEAN") {
            System.out.println(result.bValue);
        } else if(result.type == "DOUBLE") {
            System.out.println(result.dValue);
        } else if (result.type == "INTEGER") {
            System.out.println(result.iValue);
        } else {
            System.out.println(result.sValue); //Todo: maybe need to throw type error here
        }
    }

    // evaluate assignment by  updating the environment
    // returns the variable's new value
    private Lexeme evalAssingment(Lexeme tree, Lexeme env) {
        String varname = tree.left.sValue; // get the variable's name
        Lexeme value = eval(tree.left.left, env); // evaluate the value you want to set the var to
        return global.update(varname, value, env); // update the variable's value in the environment
    }

    // evaluate a variable defintion by adding the variable to the environment
    // returns the value of the new variable
    private Lexeme evalVariableDef(Lexeme tree, Lexeme env) {
        Lexeme variable = new Lexeme("VAR", tree.left.sValue); // make a new lexeme of type var with variable's name
        Lexeme value = eval(tree.left.left, env); // evaluate the expression you're setting the variable's value to (expression subtree in vardef parse tree)
        return global.insert(env, variable, value);
    }


}
