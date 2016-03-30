import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by appleowner on 3/27/16.
 */
public class recognizer {
    public Lexer lexer;
    public Lexeme currentLexeme;
    Lexeme nextLexeme;
    boolean validFile = true;

    public recognizer(String fileName) {
        lexer = new Lexer(getFileContents(fileName));
    }

    public void parse() throws Exception {
        currentLexeme = lexer.lex();
        //System.out.println("type " + currentLexeme.type);
        if (check("VARDEF")) {
            System.out.print("kjj");
            variableDef();
        } else if (check("VAR")) {
            varExpression();
        }
        if (validFile) {
            System.out.println("Legal");
        } else {
            System.out.println("Illegal");
        }
    }

    public String getFileContents (String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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

    public void advance() {
        currentLexeme = lexer.lex();
        while (currentLexeme.type == "SPACE") {
            currentLexeme = lexer.lex();
        }
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
        System.out.println("type " + type +" currentLexeme type: " + currentLexeme.type);
        if (!check(type)) {
            throw new Exception("Syntax error");
        }
    }

    /*
    * check if the current lexeme is of a given type
     */
    public boolean check(String type) {
        //System.out.println("hhhh");
        //System.out.println(currentLexeme.type);
        //System.out.println(type);
        return currentLexeme.type == type;
    }

    /*
    *     primary: variable
           | literal
           | lambdaCall
           | functionCall
     */
    public void primary() throws Exception {
        if (varExpressionPending()) {
            System.out.println("variable pending in primary fn");
            varExpression();
        } else if (literalPending()) {
            System.out.println("literal pending in primary fn");
            literal();
        } else if (lambdaCallPending()) {
            System.out.println("lambda call pending in primary fn");
            lambdaCall();
        } else if (varExpressionPending()) {

        }
    }

    /*
    * Rule 27: lambda call
    * lambdaCall: LAMBDA OPAREN paramList CPAREN
     */
    public void lambdaCall() throws Exception {
        match("LAMBDA");
        match("OPAREN");
        paramList();
        match("CPAREN");
    }

    /*
    Rule 4:
        binaryOperator: PLUS
            | MINUS
            | comparator
            | MULT
            | MULTMULT

     */
    public boolean binaryOperatorPending() {
        return check("PLUS") || check("MINUS") || check("MULT") || check("MULTMULT") || check("COMPARATOR");
    }

    public void binaryOperator() throws Exception {
        if (check("PLUS")) {
            match("PLUS");
        } else if (check("MINUS")) {
            match("MINUS");
        } else if (check("MULT")) {
            match("MULTMULT");
        } else if (check("COMPARATOR")) {
            match("COMPARATOR");
        } else {
            throw new Exception("Syntax Error: expected a binary operator");
        }
    }

    public boolean unaryOperatorPending() {
        return check("PLUSPLUS") || check("MINUSMINUS");
    }

    public void unaryOperator() throws Exception {
        if (check("PLUSPLUS")) {
            match("PLUSPLUS");
        } else if (check("MINUSMINUS")) {
            match("MINUSMINUS");
        } else {
            throw new Exception("Syntax Error: expected a unary operator");
        }
    }

    public boolean returnPending() {
        return check("RETURN");
    }

    public void returnVal() throws Exception {
        match("RETURN");
        primary();
    }

    /*
    * Rule 8: expression
    * expression: primary
          | primary operator expression
          | primary unaryOperator
          | RETURN primary
     */
    public void expression() throws Exception {
        if (primaryPending()) {
            primary();
            if (unaryOperatorPending()) {
                unaryOperator();
            } else if (binaryOperatorPending()) {
                binaryOperator();
                primary();
            }
        } else {
            new Exception("Syntax Error: invalid expression");
        }
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
            System.out.println("caught exception in literal fn");
            validFile = false;
        }
    }


    /*
    * Rule 23:
    * body: OBRACKET expressionList CBRACKET
     */
    public void body() throws Exception {
        match("OBRACKET");
        optExpressionList();
        match("CBRACKET");
    }


    /*
    * rule 25: lambda
     */
    public void lambda() throws Exception {
        match("LAMBDA");
        paramList();
        body();
    }

    /*
    Rule 22: opt expression list
    if there's an expression pending, match it and then call the fn again
    else, do nothing
     */
    public void optExpressionList() throws Exception{
        if (expressionPending()) {
            System.out.println("expression pending");
            expression();
            optExpressionList();
        }
    }

    /*
    * functionCall parse fn
    * corresponds to rule 16 in grammar
    * functionCall: varName OPAREN paramList CPAREN
     */
    public void functionCall() {
        System.out.println("in fn call");
        try {
            match("VAR"); // variable, so make call to variable fn
            System.out.println("before oparen");
            match("OPAREN");
            paramList();
            System.out.println("afterParamList");
            match("CPAREN");
            match("SEMI");
        } catch (Exception e) {
            validFile = false;
        }
    }

    /*
    * Rule 17: paramList
    * if parameter, match and then call the fn again
    * else do nothing
     */
    public void paramList() throws Exception {
        if (primaryPending()) {
            System.out.println("primary pending");
            primary();
            paramList();
        }
    }

    /*
    * Rule 15: variable
        variable: functionCall
                | lambda
                | literal
                | expression
     */
   public void variable() throws Exception {
        if (varExpressionPending()) {
            varExpressionPending();
        } else if (expressionPending()) {
            expression();
        } else if (check("VAR")) { // double check this
           match("VAR");
        } else {
            throw new Exception("not a valid variable type");
        }
    }

    public boolean varExpressionPending() {
        return check("VAR");
    }

    /*
    * rule 28: varExpression
    *     varExpression: VAR
                       | VAR OPAREN paramList CPAREN
     */
    public void varExpression() throws Exception {
        match("VAR");
        if (check("OPAREN")) { // it's a call
            match("OPAREN");
            paramList();
            match("CPAREN");
        }
    }

    /*
    * need to double check about this one
     */
    public boolean functionCallPending() {
        return check("VAR");
    }

    /*
    * see if the next lexeme is a literal
     */
    public boolean literalPending() {
        return (check("INTEGER") || check("STRING") || check("BOOLEAN") || check("DOUBLE"));
    }

    public boolean lambdaPending() {
        return check("LAMBDA");
    }

    /*
    * super not sure about this one
     */
    public boolean lambdaCallPending() {
        return check("LAMBDA");
    }

    /*
    primary: variable
           | literal
           | lambda
           | function
     */
    public boolean primaryPending() {
        return literalPending() || varExpressionPending();
    }

    public boolean expressionPending() {
        // check if a primary is pending
        return primaryPending();
    }

    /*
    * check if a variable is pending
    * variable
    variable: functionCall
            | lambda
            | literal
            | expression

        need to double/triple check this
     */
    public boolean variablePending() {
        return check("VAR");
    }

    public void variableDef() throws Exception {
        match("VARDEF");
        match("VAR");
        match("EQUAL");
        expression();
        match("SEMI");
    }

    public static void main(String[] args) throws Exception {
        recognizer rec = new recognizer("parseIn.txt");
        //rec.currentLexeme = rec.lexer.lex();
        rec.parse();
        //System.out.println();
    }
}
