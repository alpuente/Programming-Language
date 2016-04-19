import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by appleowner on 4/18/16.
 */
public class parser {
    public Lexer lexer;
    public Lexeme currentLexeme;
    boolean validFile = true;

    public parser(String fileName) {
        lexer = new Lexer(getFileContents(fileName));
    }

    public void parser() throws Exception {
        currentLexeme = lexer.lex();
        while (currentLexeme.type != "EOF") {
            if (check("DEF")) {
                functionDef();
            } else if (statementPending()) {
                statement();
            } else if (ifExpressionPending()) {
                ifExpression();
            }
            if (validFile) {
                System.out.println("Legal");
            } else {
                System.out.println("Illegal");
            }
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
        while (currentLexeme.type == "SPACE" || currentLexeme.type == "COMMENT") {
            currentLexeme = lexer.lex();
        }
    }

    /*
    * check if the lexeme is of a given type
     */
    public Lexeme match(String type) throws Exception {
        matchNoAdvance(type);
        Lexeme returnLex = currentLexeme;
        advance();
        return returnLex;
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
    public Lexeme primary() throws Exception {
        Lexeme tree;
        if (varExpressionPending()) {
            varExpression();
        } else if (literalPending()) {
            literal();
        } else if (lambdaCallPending()) {
            lambdaCall();
        } else if (varExpressionPending()) {
            varExpression();
        }
        return tree;
    }

    /*
    * Rule 27: lambda call
    * lambdaCall: LAMBDA OPAREN paramList CPAREN
     */
    public Lexeme lambdaCall() throws Exception {
        Lexeme tree;
        match("LAMBDA");
        match("OPAREN");
        paramList();
        match("CPAREN");
        return tree;
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

    public Lexeme binaryOperator() throws Exception {
        Lexeme tree;
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
        return tree;
    }

    public boolean unaryOperatorPending() {
        return check("UNIOPERATOR");
    }

    public Lexeme unaryOperator() throws Exception {
        Lexeme tree;
        if (check("PLUSPLUS")) {
            match("PLUSPLUS");
        } else if (check("MINUSMINUS")) {
            match("MINUSMINUS");
        } else {
            throw new Exception("Syntax Error: expected a unary operator");
        }
        return tree;
    }

    public boolean returnPending() {
        return check("RETURN");
    }

    public Lexeme returnVal() throws Exception {
        Lexeme tree;
        match("RETURN");
        primary();
        return tree;
    }

    /*
    * Rule 8: expression
    * expression: primary
          | primary operator expression
          | primary unaryOperator
          | RETURN primary
     */
    public Lexeme expression() throws Exception {
        Lexeme tree;
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
            arrayDeclaration();
        }
        else {
            new Exception("Syntax Error: invalid expression");
        }
        return tree;
    }

    /*
    * parse function for literal
    * corresponds to rule 1 in grammar
     */
    public Lexeme literal() {
        Lexeme tree;
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
        return  tree;
    }


    /*
    * Rule 23:
    * body: OBRACKET expressionList CBRACKET
     */
    public Lexeme body() throws Exception {
        Lexeme tree;
        match("OBRACKET");
        statementList();
        match("CBRACKET");
        return tree;
    }

    /*
    * Rule 31
    statementList: null
                 | statement
                 | statementList
 */
    public Lexeme statementList() throws Exception{
        Lexeme tree;
        if (statementPending()) {
            statement();
            statementList();
        }
        return tree;
    }


    /*
    * rule 25: lambda
     */
    public Lexeme lambda() throws Exception {
        Lexeme tree;
        match("LAMBDA");
        paramList();
        body();
        return tree;
    }

    /*
    Rule 22: opt expression list
    if there's an expression pending, match it and then call the fn again
    else, do nothing
     */
    public Lexeme optExpressionList() throws Exception{
        Lexeme tree;
        if (expressionPending()) {
            expression();
            optExpressionList();
        }
        return tree;
    }

    /*
    * Rule 17: paramList
    * if parameter, match and then call the fn again
    * else do nothing
     */
    public Lexeme paramList() throws Exception {
        Lexeme tree;
        if (primaryPending()) {
            System.out.println("primary pending");
            primary();
            paramList();
        }
        return tree;
    }

    /*
    * Rule 15: variable
        variable: functionCall
                | lambda
                | literal
                | expression
     */
    public Lexeme variable() throws Exception {
        Lexeme tree;
        if (varExpressionPending()) {
            varExpressionPending();
        } else if (expressionPending()) {
            expression();
        } else if (check("VAR")) { // double check this
            match("VAR");
        } else {
            throw new Exception("not a valid variable type");
        }
        return tree;
    }

    public boolean varExpressionPending() {
        return check("VAR");
    }

    /*
    * rule 28: varExpression
    *     varExpression: VAR
                       | VAR OPAREN paramList CPAREN
     */
    public Lexeme varExpression() throws Exception {
        Lexeme tree = match("VAR");
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
        return tree;
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

    public Lexeme variableDef() throws Exception {
        Lexeme tree = match("VARDEF");
        tree.left = match("VAR");
        match("EQUAL");
        expression();
        return tree;
    }

    public boolean paramDecPending() {
        return check("VARDEF");
    }

    /*
    * Rule 30: paramDec
    *
     */
    public Lexeme paramDec() throws Exception {
        Lexeme tree = match("VARDEF");
        tree.left = match("VAR");
        return tree;
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
    public Lexeme statement() throws Exception {
        Lexeme tree;
        if (check("RETURN")) {
            match("RETURN");
            primary();
            match("SEMI");
        } else if (expressionPending()) {
            expression();
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
        return tree;
    }

    /*
    * Rule 29: paramDecList
    *     paramDecList: null
                | paramDec
                | paramDecList
     */
    public Lexeme paramDecList() throws Exception {
        Lexeme tree;
        if (paramDecPending()) {
            tree = paramDec();
            tree.left = paramDecList();
            return tree;
        }
        return null;
    }

    /*
    * Rule 24: functionDef
    * functionDef: DEF VAR OPAREN paramDecList CPAREN body
     */
    public Lexeme functionDef() throws Exception {
        Lexeme tree = match("DEF");
        match("VAR");
        match("OPAREN");
        paramDecList();
        match("CPAREN");
        body();
        return tree;
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
    public Lexeme conditional() throws Exception {
        Lexeme tree;
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
        return tree;
    }

    /*
    * Rule 11: ifExpression
    * ifExpression: IF OPAREN conditional CPAREN body
     */
    public Lexeme ifExpression() throws Exception {
        Lexeme tree;
        match("IF");
        match("OPAREN");
        conditionalList();
        match("CPAREN");
        body();
        return tree;
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
    public Lexeme elifExpression() throws Exception {
        Lexeme tree;
        match("ELIF");
        match("OPAREN");
        conditionalList();
        match("CPAREN");
        return tree;
    }

    /*
    * Rule 12: ifChain
    * ifChain: ifExpression
       | if Expression elifChain
     */
    public Lexeme ifChain() throws Exception {
        Lexeme tree = ifExpression();
        tree.left = elifChain();
        return tree;
    }

    public boolean elseExpressionPending() {
        return check("ELSE");
    }

    /*
    * Rule 33: else expression
    * elseExpression: ELSE body
     */
    public Lexeme elseExpression() throws Exception {
        Lexeme tree = match("ELSE");
        tree.left = body();
        return tree;
    }

    /*
    * Rule 13: Elif Chain
    * elifChain: ELSE body
         | elifExpression
         | elifExpression elifChain
     */
    public Lexeme elifChain() throws Exception {
        Lexeme tree;
        if (elseExpressionPending()) {
            tree = elseExpression();
            return tree;
        } else if (elifExpressionPending()) {
            tree = elifExpression();
            tree.left = elifChain();
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
    public Lexeme whileLoop() throws Exception {
        Lexeme tree;
        match("WHILE");
        match("OPAREN");
        conditional();
        match("CPAREN");
        body();
        return tree;
    }

    /*
    * Rule 21: for loop
    * for: FOR OPAREN vardef COMMA conditional COMMA expression CPAREN body
     */
    public Lexeme forExpression() throws Exception {
        //    System.out.println("in for expression");
        Lexeme tree;
        match("FOR");
        match("OPAREN");
        variableDef();
        match("COMMA");
        conditional();
        match("COMMA");
        expression();
        match("CPAREN");
        body();
        return tree;
    }

    public boolean printPending() {
        return check("PRINT");
    }

    /*
    * Rule 34: print call
    * printCall: PRINT OPAREN primary CPAREN
     */
    public Lexeme printCall() throws Exception {
        Lexeme tree;
        match("PRINT");
        match("OPAREN");
        primary();
        match("CPAREN");
        return tree;
    }

    /*
    * Rule 35: conditional list
    *     conditionalList: conditional
                   | conditional AND conditional
                   | conditional OR conditional
     */
    public Lexeme conditionalList() throws Exception {
        Lexeme tree = conditional();
        if (check("AND")) {
            match("AND");
            conditional();
            conditionalList();
        } else if (check("OR")) {
            match("OR");
            conditional();
            conditionalList();
        }
        return tree;
    }

    public boolean arrayDeclarationPending() {
        return check("ARR");
    }

    /*
    * Rule 37: array index
    * arrIndex: VAR OSQUARE INTEGER CSQUARE
     */
    public Lexeme arrayIndex() throws Exception {
        Lexeme tree = match("VAR");
        match("OSQUARE");
        match("INTEGER");
        match("CSQUARE");
        return tree;
    }

    /*
    * Rule 36: array declaration
    * arrayDeclaration: ARR OSQUARE CSQUARE VAR
    *                 | ARR OSQUARE CSQUARE VAR EQUAL OSQUARE primaryList CSQUARE
     */
    public Lexeme arrayDeclaration() throws Exception {
        Lexeme tree = match("ARR");
        match("OSQUARE");
        match("CSQUARE");
        match("VAR");
        match("EQUAL");
        match("OSQUARE");
        if (primaryPending()) {
            primaryList();
        } else {
            match("INTEGER");
        }
        match("CSQUARE");
        return tree;
    }

    /*
    * Rule 39: primary list
    * primaryList: primary
                 | primary COMMA primaryList
     */
    public void primaryList() throws Exception {
        Lexeme tree = primary();
        if (check("COMMA")) {
            match("COMMA");
            primaryList();
        }
        return tree;
    }

    public static void main(String[] args) throws Exception {
        recognizer rec = new recognizer("parseIn.txt");
        rec.parse();
    }
}
