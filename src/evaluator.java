import java.util.ArrayList;

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
        //System.out.println("lsdjlksjlksd " + tree);
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
            case "variable_definition": // variable definition
                return evalVariableDef(tree, env);
            case "statementList":
                return evalStatementList(tree, env);
            case "statement":
                return evalStatement(tree, env);
            case "RETURN":
                return evalReturn(tree, env);
            case "PRINT":
                evalPrint(tree, env); // doesn't return lexeme
                break;
            case "INTEGER":
                return new Lexeme("INTEGER", tree.iValue); // maybe this is right? i don't think there's any case where it would have a left or right
            case "DOUBLE":
                return new Lexeme("DOUBLE", tree.dValue);
            case "STRING":
                return new Lexeme("STRING", tree.sValue);
            case "BOOLEAN":
                return new Lexeme("BOOLEAN", tree.bValue);
            case "primary":
                return evalPrimary(tree, env);
            case "expression":
                return evalExpression(tree, env);
            case "just_var":
                return evalVar(tree, env);
            case "conditional":
                return  evalConditional(tree, env);
            case "IF":
                return evalIfChain(tree, env);
            case "WHILE":
                return evalWhileLoop(tree, env);
            case "arrayDec":
                return evalArrayDec(tree, env);
            case "arrIndexAssignment":
                return evalArrayIndexAssignment(tree, env);
            case "arrIndex":
                return evalArrayIndex(tree, env);
        }
        return null;
    }

    // evaluate a function definition
    private void evalFuncDef(Lexeme tree, Lexeme env) {
        // need to make a closure and have it point to the defining environment
        Lexeme closure = new Lexeme("closure");
        closure.left = new Lexeme("join");
        closure.left.left = env; // left points to the defining environment
        closure.left.right = tree; // should point to the functionDef parse tree

        // need to double check if this is the right tree to be attaching
        String fnName = tree.left.sValue; // get the function name

        global.insert(env, new Lexeme("function_name", fnName), closure);// add the closure to the environment
        global.displayAll(env);
    }

    // evaluate a variable access by getting its value if defined
    // Todo if this works, throw exception for undefined variables
    private Lexeme evalVar(Lexeme tree, Lexeme env) {
        System.out.println("tree " + tree.type);
        //System.out.println("just_var " + tree.left.sValue);
        //System.out.println("value in justVar " + global.get(tree.left.sValue, env).type);
        Lexeme get = global.get(tree.left.sValue, env);
        Lexeme value = eval(get, env);
        //System.out.println("valllll " + value.type);
        //return global.get(tree.left.sValue, env); // use the string value of the variable name
        return value;
    }


    // evaluate a function call
    private Lexeme evalFunctionCall(Lexeme tree, Lexeme env) {
        Lexeme closure = global.get(tree.left.sValue, env); // get the function's name from parse tree
        Lexeme args = getArgList(tree); // get arg list from parse tree
        Lexeme params = getParamList(closure.left.right); // get arg list from closure, pass in the functionDef parse tree
        Lexeme body = getClosureBody(closure.left.right); // get closure body, pass in functionDef parse tree
        Lexeme senv = getStaticEnvironment(closure); // get the static environment (defining environment of fn?)
        Lexeme eval_args = evalArgList(args, env); // need to evaluate the args...// TODO is this supposed to be evalArgs specifically?
        Lexeme extended_env = global.extend(params, eval_args, senv);

        return evalBlock(body, extended_env);
    }

    // get parameters from function call tree
    private Lexeme getArgList(Lexeme tree) {
        Lexeme current_lexeme = tree.left.left; // first part of param list
        //System.out.println("type " + current_lexeme.type);
        if(current_lexeme.left != null) {
            Lexeme args = current_lexeme.left;
            //System.out.println("arg type " + args.left.type);
            current_lexeme = current_lexeme.right;
//            System.out.println(" current now " + current_lexeme.left.type);
            while (current_lexeme.left != null) { // walk through list getting args
                Lexeme temp = args;
                args = current_lexeme.left;
                args.right = temp;
                current_lexeme = current_lexeme.right;
                //System.out.println("WAHOOO " + current_lexeme.type);
            }
            return args;
        }
        return null;
    }

    private Lexeme getParamList(Lexeme tree) {
        Lexeme current_lexeme = tree.left.left; // first part of param list
        if(current_lexeme.left != null) {
            //System.out.println("current_lexeme param " + current_lexeme.left.left.type + " woah string value " + current_lexeme.left.left.sValue);
            Lexeme params = current_lexeme.left.left;
            current_lexeme = current_lexeme.right;
            while (current_lexeme.left != null) { // walk through list getting args
                //Lexeme temp = params; // have to preserve backwards ordering because that's how i'm doing it in getArgs
                //params = current_lexeme.left.left;
                params.left = current_lexeme.left.left;
                //System.out.println("current_lexeme param " + current_lexeme.left.left.type + " woah string value " + current_lexeme.left.left.sValue);
                //System.out.println("params.left " + params.left.sValue);
                current_lexeme = current_lexeme.right;
            }
            return params;
        }
        return null;
    }

    private Lexeme evalArgList(Lexeme tree, Lexeme env) {
        Lexeme arg = tree;
        //System.out.println(" arg tree type " + tree.type +  " value " + tree.type);
        Lexeme vals = null;
        while (arg != null) {
            //System.out.println("arg wasnt null");
            Lexeme temp = vals;
            //System.out.println("arg " + arg.type);
            vals = eval(arg, env); // get the variable
            vals.right = temp;
            //System.out.println("value of argument " + eval(arg.left, env));
            arg = arg.right; // move to the next variable in arg list
        }
        return vals;
    }

    private Lexeme getClosureBody(Lexeme tree) {
        return tree.left.right.left; // return body from parse tree
    }

    private Lexeme getStaticEnvironment(Lexeme closure) {
        return closure.left.left;
    }

    // need to evaluate each statement in the block's list of statements
    private Lexeme evalBlock(Lexeme tree, Lexeme env) {
        Lexeme statement_list = tree.left; // ignore brackets
        Lexeme result = evalStatementList(statement_list, env); // evaluate the statement list and return result
        return result;
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
        //System.out.println("op type " + op /*+ "equal? " + (op.contentEquals("+"))*/);
        if (op.contentEquals("+")) {
            //System.out.println("evaluating plus");
            return evalPlus(tree, env);
        } else if (op.contentEquals("*")) {
            return evalMult(tree, env);
        } else if (op.contentEquals("/")) {
            return evalDivision(tree, env);
        } else if (op.contentEquals("%")) {
            return evalMod(tree, env);
        } else if (op.contentEquals("^")) {
            return evalExponentiation(tree, env);
        } else if (op.contentEquals("-")) {
            return evalMinus(tree, env);
        }
        return null;
    }

/*    private Lexeme evalVarExpr(Lexeme tree, Lexeme env) {
    }*/

    // evaluate binary addition
    private Lexeme evalPlus(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        //System.out.println("left type " + left.type + " of value " + left.iValue);
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        //System.out.println("right type " + right.type + " of value " + right.iValue);
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", left.iValue + right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue + right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue + right.dValue);
        } else if (left.type == "STRING" && right.type == "INTEGER") {
            return new Lexeme("STRING", left.sValue + right.iValue);
        } else if (left.type == "INTEGER" && right.type == "STRING") {
            return new Lexeme("STRING", left.iValue + right.sValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue + right.dValue);
        }
    }

    private Lexeme evalMinus(Lexeme tree, Lexeme env) {
        Lexeme left = eval(tree.left, env); // evaluate the left operand
        //System.out.println("left type " + left.type + " of value " + left.iValue);
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        //System.out.println("right type " + right.type + " of value " + right.iValue);
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("INTEGER", left.iValue - right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue - right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue - right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE"){
            return new Lexeme("DOUBLE", left.dValue - right.dValue);
        } else {
            System.out.println("Error: cannot subtract types " + left.type + " and " + right.type);
            System.exit(0);
            return null;
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
        System.out.println("left type " + left.type + " of value " + left.iValue);
        Lexeme right = eval(tree.left.right.left, env); // evaluate right operand
        System.out.println("right type " + right.type + " of value " + right.iValue);
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            System.out.println("division " + (double) (left.iValue / right.iValue));
            return new Lexeme("DOUBLE",(double) left.iValue / right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("DOUBLE", left.dValue / right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("DOUBLE", left.iValue / right.dValue);
        } else {
            return new Lexeme("DOUBLE", left.dValue / right.dValue);
        }
    }

    private Lexeme evalPrimary(Lexeme tree, Lexeme env) {
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
        System.out.println("type " + tree.left.left.type);
        Lexeme value = eval(tree.left.left, env); // evaluate the value you want to set the var to
        //System.out.println("value.type " + value);
        return global.update(varname, value, env); // update the variable's value in the environment
    }

    // evaluate a variable defintion by adding the variable to the environment
    // returns the value of the new variable
    private Lexeme evalVariableDef(Lexeme tree, Lexeme env) {
        Lexeme variable = new Lexeme("VAR", tree.left.left.sValue); // make a new lexeme of type var with variable's name
        Lexeme value = eval(tree.left.left.left, env);
        Lexeme insert = global.insert(env, variable, value);
        //System.out.println("inserted " + tree.left.left.sValue + " " + insert.type);
        return insert;
    }

    public void inOrderTraversal(Lexeme tree) {
        if (tree == null) {
            return;
        }

        System.out.println(tree.type);
        inOrderTraversal(tree.left);
        inOrderTraversal(tree.right);
    }

    private Lexeme evalConditional(Lexeme tree, Lexeme env) {
        if (tree.left.type.contentEquals("primary")) { // single value that should evaluate to be a boolean
            return eval(tree.left, env); //Todo: figure this out. should be a boolean
        }
        Lexeme comparator = tree.left;
        Lexeme left = eval(tree.left.left, env); // get the primary value on the left
        Lexeme right = eval(tree.left.right, env); // get the primary value on the right
        if (comparator.sValue.contentEquals("<")) {
            return evalLessThan(left, right);
        } else if (comparator.sValue.contentEquals("<=")) {
            return evalLessEqual(left, right);
        } else if (comparator.sValue.contentEquals(">")) {
            return evalGreaterThan(left, right);
        } else if (comparator.sValue.contentEquals("=>")) {
            return evalGreaterEqual(left, right);
        } else if (comparator.sValue.contentEquals("==")) {
            return evalIsEqual(left, right);
        } else {
            System.out.println("Error: invalid conditional expression.");
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalLessThan(Lexeme left, Lexeme right) {
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.iValue < right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.dValue < right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.iValue < right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.dValue < right.dValue);
        } else {
            System.out.println("Error: cannot compare values of type " + left.type +  " and " + right.type);
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalLessEqual(Lexeme left, Lexeme right) {
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.iValue <= right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.dValue <= right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.iValue <= right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.dValue <= right.dValue);
        } else {
            System.out.println("Error: cannot compare values of type " + left.type +  " and " + right.type);
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalGreaterThan(Lexeme left, Lexeme right) {
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.iValue > right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.dValue > right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.iValue > right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.dValue > right.dValue);
        } else {
            System.out.println("Error: cannot compare values of type " + left.type +  " and " + right.type);
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalGreaterEqual(Lexeme left, Lexeme right) {
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.iValue >= right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.dValue >= right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.iValue >= right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.dValue >= right.dValue);
        } else {
            System.out.println("Error: cannot compare values of type " + left.type +  " and " + right.type);
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalIsEqual(Lexeme left, Lexeme right) {
        if (left.type == "INTEGER" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.iValue == right.iValue);
        } else if (left.type == "DOUBLE" && right.type == "INTEGER") {
            return new Lexeme("BOOLEAN", left.dValue == right.iValue);
        } else if (left.type == "INTEGER" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.iValue == right.dValue);
        } else if (left.type == "DOUBLE" && right.type == "DOUBLE") {
            return new Lexeme("BOOLEAN", left.dValue == right.dValue);
        } else {
            System.out.println("Error: cannot compare values of type " + left.type +  " and " + right.type);
            System.exit(0);
            return null;
        }
    }

    private Lexeme evalIfChain(Lexeme tree, Lexeme env) {
        if (eval(tree.left.left, env).bValue) { // evaluate an if expression
            return evalIfExpression(tree, env); // what do i return in here????
        } else {
            return evalElifChain(tree.right, env);
        }
    }

    private Lexeme evalIfExpression(Lexeme tree, Lexeme env) {
        if (eval(tree.left.left, env).bValue) {
            return evalBlock(tree.left.right, env);
        }
        else return null;
    }

    private Lexeme evalElifChain(Lexeme tree, Lexeme env) {
        Lexeme parent = tree.left;
        if (parent != null) {
            Lexeme elif = tree.left.left;
            while (parent.left != null) { // current elifChain
                if (parent.type.contentEquals("ELSE")) { // if its an else
                    return eval(tree.left.left, env); // evaluate the else statement
                } else if (parent.left.type.contentEquals("ELSE")) {
                    return eval(parent.left.left, env); // evaluate else body
                } else if (eval(elif.left.left, env).bValue) { // if the current elif should be evaluated
                    return eval(elif.left.right, env); // evaluate its body
                } else {
                    parent = parent.right;
                }
            }
        }
        return null;
    }

    protected Lexeme evalWhileLoop(Lexeme tree, Lexeme env) {
        Lexeme result = null;
        while (eval(tree.left.left, env).bValue) {
            result = evalBlock(tree.left.right, env);
        }
        return result;
    }

    protected Lexeme evalArrayDec(Lexeme tree, Lexeme env) {
        Lexeme name = new Lexeme("VAR", tree.left.left.sValue); // new lexeme with the string value of the array's name
        ArrayList<Lexeme> array = new ArrayList<>();
        Lexeme value = global.insert(env, name, new Lexeme("ARRAY", array));
        return value;
    }

    protected Lexeme evalArrayIndex(Lexeme tree, Lexeme env) {
        String array_name = tree.left.left.left.sValue; // the array's name
        ArrayList<Lexeme> array = global.get(array_name, env).aValue;
        int index = eval(tree.left.right, env).iValue; // get the index as an int
        return array.get(index); // return the object at the index
    }

    protected Lexeme evalArrayIndexAssignment(Lexeme tree, Lexeme env) {
        String array_name = tree.left.left.left.sValue;
        ArrayList<Lexeme> array = global.get(array_name, env).aValue;
        int index = eval(tree.left.right.left, env).iValue;
        if (array.size() <= index) {
            for (int i = array.size(); i <= index; i++) {
                array.add(new Lexeme("null"));
            }
        }
        return array.set(index, eval(tree.left.right.right, env));
    }
}