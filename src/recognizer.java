/**
 * Created by appleowner on 3/27/16.
 */
public class recognizer {
    Lexer lexer;
    Lexeme currentLexeme;
    boolean validFile;

    public void recognizer(String fileName) {
        lexer = new Lexer(fileName);
    }

    public void advance() {
        currentLexeme = lexer.lex();
    }

    /*
    * check if the lexeme is of a given type
     */
    public void match(String type) throws Exception {
        matchNoAdvance(type);
        advance();
    }

    /*
    * if the lexeme isn't matched, throw an exception
     */
    public void matchNoAdvance(String type) throws Exception {
        if (!check(type)) {
            throw new Exception("Syntax error");
        }
    }

    /*
    * check if the current lexeme is of a given type
     */
    public boolean check(String type) {
        return currentLexeme.type == type;
    }


/*    function primary()
    {
        if (check(NUMBER))
        {
            match(NUMBER);
        }
        else if (check(VARIABLE))
        {
            // two cases!
            match(VARIABLE);
            if (check(OPAREN))
            {
                match(OPAREN);
                optExpressionList();
                match(CPAREN);
            }
        }
        else
        {
            match(OPAREN);
            expression();
            match(CPAREN);
        }
    }*/

    public void primary() {

    }

    public void expression() {

    }

    /*
    * parse function for literal
    * corresponds to rule 1 in grammar
     */
    public void literal() {
        try {
            if (check("INTEGER")) {
                match("INTEGER");
            } else if (check("DOUBLE")) {
                match("DOUBLE");
            } else if (check("BOOLEAN")) {
                match("BOOLEAN");
            } else if (check("STRING")) {
                match("STRING");
            }
        } catch (Exception e) {
            validFile = false;
        }
    }

    public void lambda() {

    }

    /*
    * functionCall parse fn
    * corresponds to rule 16 in grammar
    * functionCall: varName OPAREN paramList CPAREN
     */
    public void functionCall() {
        try {
            match("VAR"); // variable, so make call to variable fn
            match("OPAREN");

        } catch (Exception e) {
            validFile = false;
        }
    }

    public void paramList() {

    }

    /*
    * Rule 15: variable
        variable: functionCall
                | lambda
                | literal
                | expression
     */
    public void variable() {

    }
}
