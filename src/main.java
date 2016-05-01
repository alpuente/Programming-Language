/**
 * Created by appleowner on 4/23/16.
 */
public class main {

    protected static void runEvaluationTest(String filename) {
        parser parser = new parser(filename);
        try {
            Lexeme tree = parser.parseRecursive();
            new evaluator(tree); // evaluate the tree
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void printTest(String filename) {
       new scanner(filename);
    }

    public static void main(String[] args) {
        String test_type = args[0]; // which test to evaluate
        switch (test_type) {
            case "cat-recursion":
                printTest("recursionTest.txt");
                break;
            case "run-recursion":
                runEvaluationTest("recursionTest.txt");
                break;
            case "cat-error1":
                printTest("error1.txt");
                break;
            case "run-error1":
                runEvaluationTest("error1.txt");
                break;
            case "cat-error2":
                printTest("error2.txt");
                break;
            case "run-error2":
                runEvaluationTest("error2.txt");
                break;
            case "cat-error3":
                printTest("error3.txt");
                break;
            case "run-error3":
                runEvaluationTest("error3.txt");
                break;
            case "cat-iteration":
                printTest("iteration.txt");
                break;
            case "run-iteration":
                runEvaluationTest("iteration.txt");
                break;
            case "cat-conditionals":
                printTest("conditionals.txt");
                break;
            case "run-conditionals":
                runEvaluationTest("conditionals.txt");
                break;
            case "cat-functions":
                System.out.println("functions as first class objects are not implemented");
                break;
            case "run-functions":
                System.out.println("functions as fist class objects are not implemented");
                break;
            case "cat-dictionary":
                System.out.println("did not implement dictionary");
                break;
            case "run-dictionary":
                System.out.println("did not implement dictionary");
                break;
            case "cat-problem":
                System.out.println("did not implement problem");
                break;
            case "run-problem":
                System.out.println("did not implement problem");
                break;
            case "cat-arrays":
                printTest("arrays.txt");
                break;
            case "run-arrays":
                runEvaluationTest("arrays.txt");
        }
    }
}
