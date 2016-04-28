/**
 * Created by appleowner on 4/23/16.
 */
public class main {

    public static void main(String[] args) {
        parser parser = new parser("evalTest.txt");
        try {
            Lexeme tree = parser.parseRecursive();
/*            System.out.println("in order");
            parser.inOrderTraversal(tree);
            System.out.println("in order end");*/
            new evaluator(tree); // evaluate the tree
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
