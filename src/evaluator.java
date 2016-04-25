import org.omg.CORBA.Environment;

/**
 * Created by appleowner on 4/23/16.
 */
public class evaluator {
    Lexeme e;
    environment global;

    public evaluator(Lexeme tree) {
        environment e = new environment();
        this.e = e.create();
        global = e; // idk about this ...
    }

    public Lexeme eval(Lexeme tree, Lexeme env) {
        if (tree.type == "functionDef") { // function definition
            evalFuncDef(tree, env);
        } else if (tree.type == "function_call") { // function call
            return evalFunctionCall(tree, env);
        } else if (tree.type == "block") { // a body/block
            return evalBlock(tree, env);
        } else if (tree.type == "binoperation") { // binary operator
            return evalBinaryOp(tree, env);
        } else if (tree.type == "unioperation") { // unary operator
            return evalUniOperation(tree, env);
        } else if (tree.type == "assignment") { // assignment
            evalAssingment(tree, env);
        } else if (tree.type == "VARDEF") { // variable definition

        }
    }

    // evaluate a function definition
    public void evalFuncDef(Lexeme tree, Lexeme env) {
        // need to make a closure and have it point to the defining environment
        Lexeme closure = new Lexeme("closure");
        closure.left = env; // left points to the defining environment
        closure.right = tree; // should point to the functionDef parse tree

        // need to double check if this is the right tree to be attaching
        String fnName = tree.left.left.sValue; // get the function name

        global.insert(env, new Lexeme("function_name", fnName), closure); // add the closure to the environment
    }

    // evaluate a function call
    public Lexeme evalFunctionCall(Lexeme tree, Lexeme env) {
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
    public Lexeme getArgList(Lexeme tree) {
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

    public Lexeme getParamList(Lexeme tree) {
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

    public Lexeme getClosureBody(Lexeme tree) {
        return tree.left.right.left; // return body from parse tree
    }

    public Lexeme getStaticEnvironment(Lexeme closure) {
        return closure.left;
    }

    // need to evaluate each statement in the block's list of statements
    public Lexeme evalBlock(Lexeme tree, Lexeme env) {
        Lexeme statement_list = tree.left.left; // ignore brackets
        Lexeme current_statement = statement_list.left;
        Lexeme result = null;
        while (current_statement != null) { // eval each statement in list
            result = eval(current_statement, env); // eval the statement (i think that's the statement ?)
            statement_list = statement_list.right; // move to the next statement thing
            current_statement = statement_list.left; // next statement
        }
        return result; // return value of last statement's evaluation
    }

    // evaluate each statement in a statement list
    public Lexeme evalStatementList(Lexeme tree, Lexeme env) {
        Lexeme statement_list = tree;
        Lexeme current_statement = tree.left;
        Lexeme result = null;
        while (current_statement != null) {
            result = eval(current_statement, env);
            statement_list = statement_list.right; // next statement list
            current_statement = statement_list.left; // next statement
        }
        return result;
    }

    public Lexeme evalBinaryOp(Lexeme tree, Lexeme env) {
        String op = tree.left.right.sValue;
        if (op == "+") {
            return evalPlus(tree, env);
        } else if (op == "*") {
            return evalMult(tree, env);
        } else if (op == "/") {
            return evalDivision(tree, env);
        } else if (op == "%") {
            return evalMod(tree, env);
        } else if (op == "^") {
            return evalExponentiation(tree, env);
        }
    }

    // evaluate binary addition
    public Lexeme evalPlus(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
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
    public Lexeme evalMult(Lexeme tree, Lexeme env) {
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
    public Lexeme evalDivision(Lexeme tree, Lexeme env) {
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

    // evaluate an exponentiation operation
    public Lexeme evalExponentiation(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", left.iValue ** right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", Math.pow(left.dValue, (double) right.iValue));
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", Math.pow((double) left.iValue,  right.dValue));
        } else {
            return new Lexeme("DOUBLE", Math.pow(left.dValue, right.dValue));
        }
    }

    // evaluate a mod operation
    public Lexeme evalMod(Lexeme tree, Lexeme env) {
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

    public Lexeme evalUniOperation(Lexeme tree, Lexeme env) {
        String op = tree.left.right.sValue; // get the operator as a string
        if (op == "++") {
            return evalIncrement(tree, env);
        } else if (op == "--") {
            return evalDecrement(tree, env);
        }
        return null; // idk about this...
    }

    public Lexeme evalIncrement(Lexeme tree, Lexeme env) {
        Lexeme operand = eval(tree.left, env); // evaluate the operand
        if (operand.type == "INTEGER") {
            return new Lexeme("INTEGER", operand.iValue++);
        } else { // has to be a double TODO think about throwing error here
            return new Lexeme("DOUBLE", operand.dValue++);
        }
    }

    public Lexeme evalDecrement(Lexeme tree, Lexeme env) {
        Lexeme operand = eval(tree.left, env); // evaluate the operand
        if (operand.type == "INTEGER") {
            return new Lexeme("INTEGER", operand.iValue--);
        } else {
            return new Lexeme("DOUBLE", operand.dValue--);
        }
    }

    // evaluate assignment by  updating the environment
    // returns the variable's new value
    public Lexeme evalAssingment(Lexeme tree, Lexeme env) {
        String varname = tree.left.sValue; // get the variable's name
        Lexeme value = eval(tree.left.left, env); // evaluate the value you want to set the var to
        return global.update(varname, value, env); // update the variable's value in the environment
    }

    // evaluate a variable defintion by adding the variable to the environment
    // returns the value of the new variable
    public Lexeme evalVariableDef(Lexeme tree, Lexeme env) {
        Lexeme variable = new Lexeme("VAR", tree.left.sValue); // make a new lexeme of type var with variable's name
        Lexeme value = eval(tree.left.left, env); // evaluate the expression you're setting the variable's value to (expression subtree in vardef parse tree)
        return global.insert(env, variable, value);
    }


}
