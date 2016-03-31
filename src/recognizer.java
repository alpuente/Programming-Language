import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

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
        } else if (check("DEF")) {
            functionDef();
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
            varExpression();
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
        return check("UNIOPERATOR");
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
            if (check("UNIOPERATOR")) {
                match("UNIOPERATOR");
            } else if (check("BINOPERATOR")) {
                match("BINOPERATOR");
                primary();
            }
        } else if (varDefPending()) {
            variableDef();
        } else if (check("ARR")) {
            System.out.println("arr");
            arrayDeclaration();
        }
        else {
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
        statementList();
        match("CBRACKET");
    }

    /*
* Rule 31
statementList: null
             | statement
             | statementList

 */
    public void statementList() throws Exception{
        System.out.println("statement " + currentLexeme.type);
        if (statementPending()) {
            System.out.println("statement pending");
            statement();
            statementList();
        }
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
        } else if (check("EQUAL")) { // reassignment
            match("EQUAL");
            primary();
        } else if (check("OSQUARE")) {
            arrayIndex();
        }
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

        return primaryPending() || check("VARDEF") || check("RETURN") || check("ARR");
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
    }

    public boolean paramDecPending() {
        System.out.println("type " + currentLexeme.type);
        //System.out.println(check("VARDEF"));
        return check("VARDEF");
    }

    /*
    * Rule 30: paramDec
    *
     */
    public void paramDec() throws Exception {
        match("VARDEF");
        System.out.println("CURRENT TYPE " + currentLexeme.type);
        match("VAR");
    }

    /*
    * check if a statement is pending
    */
    public boolean statementPending() {
        return expressionPending() || check("FOR") || check("WHILE") || ifExpressionPending() || printPending();
    }

    /*
    * Rule 32: statement
    *     statement: expression SEMI
             | RETURN primary SEMI
     */
    public void statement() throws Exception {
        System.out.println("in statement");
        if (check("RETURN")) {
            System.out.println("return");
            match("RETURN");
            System.out.println("return");
            primary();
            System.out.println("return");
            match("SEMI");
        } else if (expressionPending()) {
            System.out.println("stateme nt " + currentLexeme.type);
            expression();
            System.out.println("current lex " + currentLexeme.type);
            match("SEMI");
        } else if (ifExpressionPending()) {
            ifExpression();
        } else if (whilePending()) {
            whileLoop();
        } else if (forPending()) {
            forExpression();
        } else if (printPending()) {
            printCall();
            match("SEMI");
        }

    }

    /*
    * Rule 29: paramDecList
    *     paramDecList: null
                | paramDec
                | paramDecList
     */
    public void paramDecList() throws Exception {
        if (paramDecPending()) {
            paramDec();
            paramDecList();
        }
    }

    /*
    * Rule 24: functionDef
    * functionDef: DEF VAR OPAREN paramDecList CPAREN body
     */
    public void functionDef() throws Exception {
        System.out.println("before def");
        match("DEF");
        System.out.println("before var");
        match("VAR");
        System.out.println("before oparen");
        match("OPAREN");
        System.out.println("before paramDecList");
        paramDecList();
        System.out.println("before CPAREN");
        match("CPAREN");
        System.out.println("before body");
        body();
        System.out.println("after body");
    }

    public boolean varDefPending() {
        return check("VARDEF");
    }

    public boolean notPending() {
        return check("NOT");
    }

    /*
    * Rule 10: conditional
     */
    public void conditional() throws Exception {
        if (primaryPending()) {
            primary();
            match("COMPARATOR");
            primary();
        } else if (check("NOT")) {
            match("NOT");
            match("OPAREN");
            primary();
            match("COMPARATOR");
            primary();
            match("CPAREN");
        }
    }

    /*
    * Rule 11: ifExpression
    * ifExpression: IF OPAREN conditional CPAREN body
     */
    public void ifExpression() throws Exception {
        match("IF");
        match("OPAREN");
        conditionalList();
        match("CPAREN");
        body();
    }

    public boolean ifExpressionPending() {
        return check("IF");
    }

    public boolean elifExpressionPending() {
        return check("ELIF");
    }

    /*
    * Rule 32: elifExpression
    * elifExpression: ELIF OPAREN conditional CPAREN
     */
    public void elifExpression() throws Exception {
        match("ELIF");
        match("OPAREN");
        conditionalList();
        match("CPAREN");
    }

    /*
    * Rule 12: ifChain
    * ifChain: ifExpression
       | if Expression elifChain
     */
    public void ifChain() throws Exception {
        ifExpression();
        elifChain();
    }

    public boolean elseExpressionPending() {
        return check("ELSE");
    }

    /*
    * Rule 33: else expression
    * elseExpression: ELSE body
     */
    public void elseExpression() throws Exception {
        match("ELSE");
        body();
    }

    /*
    * Rule 13: Elif Chain
    * elifChain: ELSE body
         | elifExpression
         | elifExpression elifChain
     */
    public void elifChain() throws Exception {
        if (elseExpressionPending()) {
            elseExpression();
        } else if (elifExpressionPending()) {
            elifExpression();
            elifChain();
        }
    }

    public boolean conditionalPending() {
        return primaryPending() || check("NOT");
    }

    public boolean whilePending() {
        return check("WHILE");
    }

    public boolean forPending() {
        return check("FOR");
    }

    /*
    * Rule 20: while loop
    * whileLoop: WHILE OPAREN conditional CPAREN body
     */
    public void whileLoop() throws Exception {
        match("WHILE");
        match("OPAREN");
        conditional();
        match("CPAREN");
        body();
    }

    /*
    * Rule 21: for loop
    * for: FOR OPAREN vardef COMMA conditional COMMA expression CPAREN body
     */
    public void forExpression() throws Exception {
        System.out.println("in for expression");
        match("FOR");
        match("OPAREN");
        variableDef();
        match("COMMA");
        conditional();
        match("COMMA");
        expression();
        match("CPAREN");
        body();
    }

    public boolean printPending() {
        return check("PRINT");
    }

    /*
    * Rule 34: print call
    * printCall: PRINT OPAREN primary CPAREN
     */
    public void printCall() throws Exception {
        match("PRINT");
        match("OPAREN");
        primary();
        match("CPAREN");
    }

    /*
    * Rule 35: conditional list
    *     conditionalList: conditional
                   | conditional AND conditional
                   | conditional OR conditional
     */
    public void conditionalList() throws Exception {
        conditional();
        if (check("AND")) {
            match("AND");
            conditional();
            conditionalList();
        } else if (check("OR")) {
            match("OR");
            conditional();
            conditionalList();
        }
    }

    public boolean arrayDeclarationPending() {
        return check("ARR");
    }

    /*
    * Rule 37: array index
    * arrIndex: VAR OSQUARE INTEGER CSQUARE
     */
    public void arrayIndex() throws Exception {
        match("VAR");
        match("OSQUARE");
        match("INTEGER");
        match("CSQUARE");
    }

    /*
    * Rule 36: array declaration
    * arrayDeclaration: ARR OSQUARE CSQUARE VAR
     */
    public void arrayDeclaration() throws Exception {
        match("ARR");
        match("OSQUARE");
        match("CSQUARE");
        match("VAR");
    }

    public void varForExpressionFn() throws Exception {
        match("VAR");

    }

    public static void main(String[] args) throws Exception {
        recognizer rec = new recognizer("parseIn.txt");
        //rec.currentLexeme = rec.lexer.lex();
        rec.parse();
        //System.out.println();
    }
}
