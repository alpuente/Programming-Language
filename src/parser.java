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

    public Lexeme parse() throws Exception {
        System.out.println("hey");
        currentLexeme = lexer.lex();
        Lexeme tree = new Lexeme("Program");
        int i = 0;
        while (currentLexeme.type != "EOF") {
            //System.out.println(i);
            i += 1;
            try {
                if (check("DEF")) {
                    System.out.println("boop");
                    tree.left = functionDef();
                } else if (statementPending()) {
                    System.out.print("beep");
                    tree.left = statement();
                } else if (ifExpressionPending()) {
                    System.out.println("bop");
                    tree.left = ifExpression();
                }
            } catch (Exception e) {
                validFile = false;
                break;
            }
        }
        System.out.println();
        if (validFile) {
            System.out.println("Legal");
        } else {
            System.out.println("Illegal");
        }
        return tree;
    }

    public Lexeme parseRecursive() throws Exception {
        currentLexeme = lexer.lex();
        return statementList();
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
        // not using tree variable here because they're all single lexemes
        // don't need left and right
        Lexeme tree = new Lexeme("primary");
        if (varExpressionPending()) {
            tree.right = varExpression();
            System.out.println("<primary>");
            System.out.println(tree.type);
            System.out.println(tree.right.type);
            inOrderTraversal(tree.right);
            System.out.println("</primary>");
            return tree;
        } else if (literalPending()) {
            tree.right = literal();
            return tree;
        } else if (varExpressionPending()) {
            tree.right =  varExpression();
            return tree;
        }
        return null;
    }

    /*
    * Rule 27: lambda call
    * lambdaCall: LAMBDA OPAREN paramList CPAREN
     */
    public void lambdaCall() throws Exception { // TODO: 4/19/16 fix these dang lambda calls
        Lexeme tree;
        match("LAMBDA");
        match("OPAREN");
        paramList();
        match("CPAREN");
        //return tree;
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
        if (check("PLUS")) {
            return match("PLUS");
        } else if (check("MINUS")) {
            return match("MINUS");
        } else if (check("MULT")) {
            return match("MULTMULT");
        } else if (check("COMPARATOR")) {
            return match("COMPARATOR");
        } else {
            throw new Exception("Syntax Error: expected a binary operator");
        }
    }

    public boolean unaryOperatorPending() {
        return check("UNIOPERATOR");
    }

    public Lexeme unaryOperator() throws Exception {
        // no need for tree variable here
        if (check("PLUSPLUS")) {
            return match("PLUSPLUS");
        } else if (check("MINUSMINUS")) {
            return match("MINUSMINUS");
        } else {
            throw new Exception("Syntax Error: expected a unary operator");
        }
    }

    public boolean returnPending() {
        return check("RETURN");
    }

    public Lexeme returnVal() throws Exception {
        Lexeme tree;
        tree = match("RETURN");
        tree.left = primary();
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
            tree = primary();
            if (check("UNIOPERATOR")) {
                tree.left = match("UNIOPERATOR");
                return tree;
            } else if (check("BINOPERATOR")) {
                tree.left = match("BINOPERATOR");
                tree.left.left = primary();
                return tree;
            }
        } else if (varDefPending()) {
            return variableDef();
        } else if (check("ARR")) {
            return arrayDeclaration();
        }
        else {
            new Exception("Syntax Error: invalid expression");
        }
        return null;
    }

    /*
    * parse function for literal
    * corresponds to rule 1 in grammar
     */
    public Lexeme literal() {
        Lexeme tree;
        try {
            if (check("INTEGER")) {
                return match("INTEGER");
            } else if (check("DOUBLE")) {
                return match("DOUBLE");
            } else if (check("BOOLEAN")) {
                return match("BOOLEAN");
            } else if (check("STRING")) {
                return match("STRING");
            }
        } catch (Exception e) {
            System.out.println("caught exception in literal fn");
            validFile = false;
        }
        return  null;
    }


    /*
    * Rule 23:
    * body: OBRACKET expressionList CBRACKET
     */
    public Lexeme body() throws Exception {
        Lexeme tree;
        tree = match("OBRACKET");
        Lexeme temp = statementList();
        tree.left = match("CBRACKET");
        tree.left.left = temp;
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
            tree = statement();
            tree.left = statementList();
            return tree;
        }
        return null;
    }


    /*
    * rule 25: lambda
     */
    public Lexeme lambda() throws Exception {
        Lexeme tree;
        tree = match("LAMBDA");
        tree.left = new Lexeme("dummy");
        tree.left.left = paramList();
        tree.left.right = body();
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
            tree = expression();
            tree.left = optExpressionList();
            return tree;
        }
        return null;
    }

    /*
    * Rule 17: paramList
    * if parameter, match and then call the fn again
    * else do nothing
     */
    public Lexeme paramList() throws Exception {
        Lexeme tree;
        if (primaryPending()) {
            //System.out.println("primary pending");
            tree = primary();
            tree.left = paramList();
            return tree;
        }
        return null;
    }

    /*
    * Rule 15: variable
        variable: functionCall
                | lambda
                | literal
                | expression
     */ // TODO: 4/19/16 deal with this stuff (variable fn)
/*    public Lexeme variable() throws Exception {
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
    }*/

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
            tree.left = match("OPAREN");
            tree.left.left = paramList();
            tree.left.right = match("CPAREN");
        } else if (check("EQUAL")) { // reassignment
            tree.right = match("EQUAL");
            tree.left = primary();
        } else if (check("OSQUARE")) {
            tree.left = arrayIndex();
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
        tree.left.right = match("EQUAL");
        tree.left.left = expression();
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
        return expressionPending() || check("FOR") || check("WHILE") || ifExpressionPending() || printPending() || check("DEF") || check("LAMBDA");
    }

    /*
    * Rule 32: statement
    *     statement: expression SEMI
             | RETURN primary SEMI
     */
    public Lexeme statement() throws Exception {
        Lexeme tree;
        if (check("RETURN")) {
            tree = match("RETURN");
            Lexeme temp = primary();
            tree.left = match("SEMI");
            tree.left.right  = temp;
            return tree;
        } else if (expressionPending()) {
            // TODO: 4/19/16 figure this out
            Lexeme temp = expression();
            tree = match("SEMI");
            tree.left = temp;
            return tree;
        } else if (ifExpressionPending()) {
            tree = ifChain();
            return tree;
        } else if (whilePending()) {
            tree = whileLoop();
            return tree;
        } else if (forPending()) {
            tree = forExpression();
            return tree;
        } else if (printPending()) {
            tree = printCall();
            match("SEMI");
            return tree;
        } else if (check("DEF")) {
            tree = functionDef();
        }
        return null;
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
        System.out.println("lskjd");
        Lexeme tree = match("DEF");
        tree.left = match("VAR");
        tree.left.right = match("OPAREN");
        tree.left.left = paramDecList();
        tree.right = match("CPAREN");
        tree.right.left = body();
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
        System.out.println("<conditional>");
        Lexeme tree = new Lexeme("conditonal");
        if (primaryPending()) {
            tree.left = primary();
            if (check("COMPARATOR")) {
                tree.left.left = match("COMPARATOR");
                tree.left.left.left = primary();
            }
            System.out.println("</conditional>");
            return tree;
        } else if (check("NOT")) {
            tree =  match("NOT");
            tree.left = match("OPAREN");
            tree.left.left = primary();
            tree.left.right = match("COMPARATOR");
            tree.left.right.left = primary();
            tree.left.right.right = match("CPAREN");
            System.out.println("</conditional>");
            return tree;
        }
        return null;
    }

    /*
    * Rule 11: ifExpression
    * ifExpression: IF OPAREN conditional CPAREN body
     */
    public Lexeme ifExpression() throws Exception {
        System.out.println("<ifExpression>");
        Lexeme tree = match("IF");
        tree.left = match("OPAREN");
        tree.left.left = conditionalList();
        tree.left.right = match("CPAREN");
        tree.left.right.left = body();
        inOrderTraversal(tree);
        System.out.println("</ifExpression>");
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
        Lexeme tree = match("ELIF");
        tree.left = match("OPAREN");
        tree.left.left = conditionalList();
        tree.left.right = match("CPAREN");
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
        return null;
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
        tree = match("WHILE");
        tree.left = match("OPAREN");
        tree.left.left = conditional();
        tree.left.right = match("CPAREN");
        tree.left.right.left = body();
        return tree;
    }

    /*
    * Rule 21: for loop
    * for: FOR OPAREN vardef COMMA conditional COMMA expression CPAREN body
     */
    public Lexeme forExpression() throws Exception {
        //    System.out.println("in for expression");
        Lexeme tree;
        tree = match("FOR");
        tree.left = match("OPAREN");
        tree.left.left = variableDef();
        tree.left.right = match("COMMA");
        tree.left.left.left  = conditional();
        tree.left.left.right = match("COMMA");
        tree.left.right.left = expression();
        tree.left.right.right = match("CPAREN");
        tree.left.right.right.left = body();
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
        System.out.println("<printCall>");
        Lexeme tree;
        tree = match("PRINT");
        tree.left = match("OPAREN");
        tree.left.left = primary();
        tree.left.right = match("CPAREN");
        inOrderTraversal(tree);
        System.out.println("</printCall>");
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
            System.out.println("heyyy");
            tree.left = match("AND");
            tree.left.left = conditional();
            tree.left.right = conditionalList();
        } else if (check("OR")) {
            System.out.println("lalalalososod");
            tree.left = match("OR");
            tree.left.left = conditional();
            tree.left.right = conditionalList();
        }
        System.out.println("<conditionalList>" );
        inOrderTraversal(tree);
        System.out.println("</conditionalList>");
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
        tree.left = match("OSQUARE");
        //match("INTEGER");
        tree.left.left = primary(); // i guess this should be a primary ????
        tree.left.right = match("CSQUARE");
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
    public Lexeme primaryList() throws Exception {
        Lexeme tree = primary();
        if (check("COMMA")) {
            tree.left = match("COMMA");
            tree.left.left = primaryList();
            return tree;
        }
        return null;
    }

    public void inOrderTraversal(Lexeme tree) {
        if (tree == null) {
            return;
        }

        inOrderTraversal(tree.left);
        System.out.println(tree.type);
        inOrderTraversal(tree.right);
    }

    public static void main(String[] args) throws Exception {
        parser p = new parser("parseIn.txt");
        try {
            Lexeme tree = p.parseRecursive();
            p.inOrderTraversal(tree);
        } catch (Exception e) {
            System.out.println("illegal");
            e.printStackTrace();
        }
        //p.inOrderTraversal(tree);
        /*Lexeme tree = new Lexeme("program");
        tree.left = new Lexeme("left1");
        tree.left.left = new Lexeme("left2");
        tree.left.right = new Lexeme("left1right1");
        tree.right = new Lexeme("right1");
        p.inOrderTraversal(tree);*/
    }
}
