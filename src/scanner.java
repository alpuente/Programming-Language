import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by appleowner on 3/21/16.
 */
public class scanner {

/*    function scanner(filename)
    {
        var token;
        var i = new lexer(fileName);
        3
        token = i.lex();
        while (token . type != ENDofINPUT)
        {
            System.out.println(token);
            token = i.lex();
        }
    }*/

    public String getFileContents (String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("input.txt"));
            String contents = "";
            String line;
            while ((line = br.readLine()) != null) {
                contents += line;
            }
            return contents;
        } catch (IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public scanner(String fileName) {
        Lexeme token;
        Lexer lexer = new Lexer(getFileContents(fileName));
    }

    public static void main(String[] args) {
        new scanner(args[1]); // create new scanner using the input filename
    }
}
