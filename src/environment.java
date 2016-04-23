import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by appleowner on 4/23/16.
 */
public class environment {
    protected Lexeme env;
    protected environment() {
        this.env = new Lexeme("env");
    }

    // add new lists to the environment
    // TODO: need to figure out what to return here
    protected void create() {
        Lexeme oldLeft = env.left;
        Lexeme oldRight = env.right;
        env.left = new Lexeme("varList");
        env.right = new Lexeme("valList");
        env.left.right = oldLeft;
        env.right.right = oldRight;
    }

    // insert new value and variable into the first lists in the environment
    // returns the newly inserted value
    protected Lexeme addLocalVariable(Lexeme id, Lexeme value) {
        Lexeme oldVar = env.left.left;
        Lexeme oldVal = env.right.left;
        env.left.left = id;
        env.left.left.left = oldVar;
        env.right.left.left = oldVal;
        return value;
    }

    // searches for a variable in the environment using the variable's string id
    // returns the variable's value if found or null if not
    protected Lexeme search(String id) {
        Lexeme currentVarList = env.left;
        Lexeme currentValList = env.right;
        while(currentVarList != null) {
            Lexeme val = searchList(currentVarList, currentValList, id);
            if (val != null) {
                return val;
            }
            currentValList = currentValList.right;
            currentVarList = currentVarList.right;
        }
        return null;
    }

    protected Lexeme searchList(Lexeme varList, Lexeme valList, String id) {
        Lexeme currentVar = varList.left;
        Lexeme currentVal = valList.left;
        while (currentVar != null) {
            if (currentVar.type == id) {
                return currentVal;
            } else {
                currentVal = currentVal.left;
                currentVar = currentVar.left;
            }
        }
        return null;
    }

    protected Lexeme update(String id, Lexeme value) {
        Lexeme currentVarList = env.left;
        Lexeme currentValList = env.right;
        while(currentVarList != null) {
            Lexeme val = searchAndUpdate(currentVarList, currentValList, id, value);
            if (val != null) {
                return val;
            }
            currentValList = currentValList.right;
            currentVarList = currentVarList.right;
        }
        return null;
    }

    protected Lexeme searchAndUpdate(Lexeme varList, Lexeme valList, String id, Lexeme newValue) {
        Lexeme currentVar = varList.left;
        Lexeme currentVal = valList.left;
        while (currentVar != null) {
            if (currentVar.type == id) {
                return currentVal;
                Lexeme remainingVals = currentVal.left;
                
            } else {
                currentVal = currentVal.left;
                currentVar = currentVar.left;
            }
        }
        return null;
    }
}
