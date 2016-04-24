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
        if (tree.type == "functionDef") {
            evalFuncDef(tree, env);
        } else if (tree.type == "function_call") {
            return evalFunctionCall(tree, env);
        } else if (tree.type == "block") {
            return evalBlock(tree, env);
        }
    }

    public void evalFuncDef(Lexeme tree, Lexeme env) {
        // need to make a closure and have it point to the defining environment
        Lexeme closure = new Lexeme("closure");
        closure.left = env; // left points to the defining environment
        closure.right = tree; // should point to the functionDef parse tree

        // need to double check if this is the right tree to be attaching
        String fnName = tree.left.left.sValue; // get the function name

        global.insert(env, new Lexeme("function_name", fnName), closure); // add the closure to the environment
    }

    public Lexeme evalFunctionCall(Lexeme tree, Lexeme env) {
        Lexeme closure = global.get(tree.left.sValue, env); // get the function's name from parse tree
        Lexeme args = getArgList(tree); // get arg list from parse tree
        Lexeme params = getParamList(closure.right); // get arg list from closure, pass in the functionDef parse tree
        Lexeme body = getClosureBody(closure.right); // get closure body, pass in functionDef parse tree
        Lexeme senv = getStaticEnvironment(closure); // get the static environment (defining environment of fn?)
        Lexeme eval_args = evalArgs(args, env); // need to evaluate the args...
        Lexeme extended_env = global.extend(params, eval_args, env);

        return eval(body, extended_env);
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

    public void runEval() {

    }
}
