import com.sun.deploy.Environment;
import com.sun.xml.internal.bind.v2.TODO;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by appleowner on 4/23/16.
 */
public class environment {

    // insert new value and variable into the first lists in the environment
    // returns the newly inserted value
    protected Lexeme addLocalVariable(Lexeme env, Lexeme id, Lexeme value) {
        Lexeme tempVars = env.left;
        Lexeme tempVals = env.right;
        Lexeme nextEnv = env.right.right;
        env.left = id;
        id.left = tempVars;
        env.right = value;
        env.right.left = tempVals;
        env.right.right = nextEnv;
        return value;
    }

    protected Lexeme setCar(Lexeme envir, Lexeme value) {
        value.left = envir;
        return value;
    }

    protected Lexeme setCdr(Lexeme envir, Lexeme value) {
        envir.right = value;
        return envir;
    }

    // searches for a variable in the environment using the variable's string id
    // returns the variable's value if found or null if not
    protected Lexeme get(String id, Lexeme env) {
        if (env == null) {
            return null;
        }
        Lexeme currentVar = env.left;
        Lexeme currentVal = env.right.left;
        System.out.println("id " +  id);
        while(currentVar != null) {
            if (currentVar.sValue.contentEquals(id)) {
                System.out.println("current val " + currentVal);
                return currentVal;
            }
            currentVal = currentVal.left;
            currentVar = currentVar.left;
        }
        return get(id, env.right.right); // search the next environment
    }

    protected Lexeme update(String id, Lexeme value, Lexeme env) {
        //System.out.println("id in update" + id);
        if (env == null) {
            return null;
        }
        Lexeme currentVar = env.left;
        Lexeme currentVal = env.right.left;
        //System.out.println("current var.type " + currentVar.type);
        //System.out.println("current var " + currentVar.sValue);
        if (currentVar.sValue.contentEquals(id)) {
            //System.out.println("hey");
            Lexeme temp = currentVal.left;
            env.right.left = value;
            value.left = temp;
            return value;
        }
        Lexeme nextVar = env.left.left;
        Lexeme nextVal = env.right.left.left;
        while(currentVar != null) {
            if (nextVar.sValue.contentEquals(id)) {
                value.left = nextVal.left;
                currentVal.left = value;
                return value;
            }
            currentVal = currentVal.left;
            currentVar = currentVar.left;
            nextVal = nextVal.left;
            nextVar = nextVar.left;
        }
        return update(id, value, env.right.right); // search the next environment
    }

    protected Lexeme create() {
        Lexeme cool_new_environment = new Lexeme("env");
        cool_new_environment.right = new Lexeme("values_join");
        return cool_new_environment;
    }

    protected Lexeme insert(Lexeme env, Lexeme variable, Lexeme value) {
        variable.left = env.left;
        value.left = env.right.left;
        env.left = variable;
        env.right.left = value;
        return value;
    }

    protected Lexeme extend(Lexeme varList, Lexeme valList, Lexeme env) {
        Lexeme newEnv = new Lexeme("env");
        newEnv.left = varList;
        newEnv.right = new Lexeme("vals");
        newEnv.right.left = valList;
        newEnv.right.right = env;
        return newEnv;
    }

    protected void displayLocal(Lexeme env) {
        Lexeme currentVar = env.left;
        Lexeme currentVal = env.right.left;
        while (currentVal != null) {
            System.out.println(currentVar.type + " " + currentVal.type);
            currentVar = currentVar.left;
            currentVal = currentVal.left;
        }
    }

    protected void displayAll(Lexeme env) {
        Lexeme currentEnv = env;
        while (env != null) {
            displayLocal(env);
            env = env.right.right;
        }
    }
}
