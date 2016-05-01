import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Amy Puente on 4/18/16.
 */
public class parser {
    public Lexer lexer;
    public Lexeme currentLexeme;
    boolean validFile = true;

    public parser(String fileName) {
        lexer = new Lexer(getFileContents(fileName));
    }

    public Lexeme parse() throws Exception {
        try {
            Lexeme tree = parseRecursive();
            //inOrderTraversal(tree);
        } catch (Exception e) {
            System.out.println("illegal");
            e.printStackTrace();
        }
        return null;
    }

    public Lexeme parseRecursive() throws Exception {
        advance();
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
            if (currentLexeme.type == "EOF") {
                //System.out.println("booop");
                break;
            }
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
        try {
            Lexeme tree = new Lexeme("primary");
            if (varExpressionPending()) {
                //System.out.println("var expression pending");
                return varExpression();
            } else if (literalPending()) {
                tree.left = literal();
                return tree;
            }
        } catch (Exception e) {
            System.out.println("Error: invalid syntax. Expected a primary.");
            System.exit(0);
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
        return check("BINOPERATOR"); //check("PLUS") || check("MINUS") || check("MULT") || check("MULTMULT") || check("COMPARATOR");
    }

    public Lexeme binaryOperator() throws Exception {
        try {
            if (check("PLUS")) {
                return match("PLUS");
            } else if (check("MINUS")) {
                return match("MINUS");
            } else if (check("MULT")) {
                return match("MULTMULT");
            } else if (check("COMPARATOR")) {
                return match("COMPARATOR");
            }
        } catch (Exception e) {
            System.out.println("Error: invalid syntax. Expected a binary operator.");
            System.exit(0);
        }
        return null;
    }

    public boolean unaryOperatorPending() {
        return check("UNIOPERATOR");
    }

    public Lexeme unaryOperator() throws Exception {
        // no need for tree variable here
        try {
            if (check("PLUSPLUS")) {
                return match("PLUSPLUS");
            } else if (check("MINUSMINUS")) {
                return match("MINUSMINUS");
            }
        } catch (Exception e) {
            System.out.println("Error: expected a unary operator.");
        }
        return null;
    }

    public boolean returnPending() {
        return check("RETURN");
    }

    public Lexeme returnVal() throws Exception {
        try {
            Lexeme tree;
            tree = match("RETURN");
            tree.left = primary();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid return expression.");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 8: expression
    * expression: primary
          | primary operator expression
          | primary unaryOperator
          | RETURN primary
     */
    public Lexeme expression() throws Exception {
        try {
            Lexeme tree = new Lexeme("expression");
            //System.out.println("in expr");
            if (primaryPending()) {
                //System.out.println("primary pending");
                tree.left = primary();
                if (check("UNIOPERATOR")) {
                    Lexeme temp = tree.left;
                    tree = new Lexeme("unioperation");
                    tree.left = temp;
                    tree.left.right = match("UNIOPERATOR");
                    return tree;
                } else if (check("BINOPERATOR")) {
                    Lexeme temp = tree.left;
                    tree = new Lexeme("binoperation");
                    tree.left = temp;
                    tree.left.right = match("BINOPERATOR");
                    tree.left.right.left = primary();
                    return tree;
                }
                return tree;
            } else if (varDefPending()) {
                tree = new Lexeme("variable_definition");
                tree.left = variableDef();
                return tree;
            } else if (check("ARR")) {
                tree.left = arrayDeclaration();
                return tree;
            }
        } catch (Exception e) {
            System.out.println("Error: invalid expression");
            System.exit(0);
        }
        return null;
    }

    /*
    * parse function for literal
    * corresponds to rule 1 in grammar
     */
    public Lexeme literal() {
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
            System.exit(0);
        }
        return  null;
    }


    /*
    * Rule 23:
    * body: OBRACKET expressionList CBRACKET
     */
    public Lexeme body() throws Exception {
        try {
            Lexeme tree = new Lexeme("block");
            tree.left = match("OBRACKET");
            tree.left.left = statementList();
            tree.left.right = match("CBRACKET");
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid syntax in body.");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 31
    statementList: null
                 | statement
                 | statementList
 */
    public Lexeme statementList() throws Exception{
        try {
            Lexeme tree = new Lexeme("statementList");
            if (statementPending()) {
                tree.left = statement();
                tree.right = statementList();
                return tree;
            }
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid statement list");
            System.exit(0);
        }
        return null;
    }


    /*
    * rule 25: lambda
     */
    public Lexeme lambda() throws Exception {
        try {
            Lexeme tree;
            tree = match("LAMBDA");
            tree.left = new Lexeme("dummy");
            tree.left.left = paramList();
            tree.left.right = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid lambda syntax.");
            System.exit(0);
        }
        return null;
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
        try {
            Lexeme tree = new Lexeme("paramList");
            //System.out.println("hey");
            //System.out.println(primaryPending());
            if (primaryPending()) {
                //System.out.println("primary pending");
                tree.left = primary();
                tree.right = paramList();
                return tree;
            }
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid syntax in parameter list.");
            System.exit(0);
        }
        return null;
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
        try {
            Lexeme tree = new Lexeme("VAREXPR"); // make dummy type node
            tree.left = match("VAR");
            Lexeme top = new Lexeme("primary");
            if (check("OPAREN")) { // it's a call
                //System.out.println("function call");
                match("OPAREN");
                Lexeme temp = tree.left;
                tree = new Lexeme("function_call");
                tree.left = temp;
                tree.left.left = paramList();
                top.left = tree;
                match("CPAREN");
                return top; // make this a primary
                //tree.left.right.left = match("CPAREN");
            } else if (check("EQUAL")) { // reassignment
                //System.out.println("assignment");
                match("EQUAL");
                Lexeme temp = tree.left;
                tree = new Lexeme("assignment");
                tree.left = temp;
                tree.left.left = primary();
                return tree;
            } else if (check("OSQUARE")) {
/*            tree.left = arrayIndex();
            top.left = tree;*/
                top.left = arrayExpression(tree.left); // make an array expression, passing in the array name (previously acquired VAR lexeme)
                return top;
            } else if (binaryOperatorPending()) {
                //Lexeme temp = tree.left;
                Lexeme jv = new Lexeme("just_var");
                jv.left = tree.left;
                tree = new Lexeme("binoperation");
                tree.left = jv;
                tree.left.right = match("BINOPERATOR");
                tree.left.right.left = primary();
                top.left = tree;
                return top;
            } else if (check("COMPARATOR")) {
                Lexeme prim = new Lexeme("primary");
                Lexeme jv = new Lexeme("just_var");
                jv.left = tree.left;
                prim.left = jv;
                top = new Lexeme("conditional");
                top.left = match("COMPARATOR");
                top.left.left = prim;
                top.left.right = primary();
                return top;
            } else { // just a variable and the evaluater will need to grab its value
                Lexeme jv = new Lexeme("just_var");
                jv.left = tree.left;
                top.left = jv;
                return jv;
            }
        } catch (Exception e) {
            System.out.println("Error: invalid variable expression");
        }
        return null;
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
        try {
            Lexeme tree = match("VARDEF");
            tree.left = match("VAR");
            tree.left.right = match("EQUAL");
            tree.left.left = expression();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid variable defintion");
            System.exit(0);
        }
        return null;
    }

    public boolean paramDecPending() {
        return check("VARDEF");
    }

    /*
    * Rule 30: paramDec
    *
     */
    public Lexeme paramDec() throws Exception {
        try {
            Lexeme tree = match("VARDEF");
            tree.left = match("VAR");
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid parameter declaration");
            System.exit(0);
        }
        return null;
    }

    /*
    * check if a statement is pending
    */
    public boolean statementPending() {
        return expressionPending() || check("FOR") || check("WHILE") || ifExpressionPending() || printPending() || check("DEF") || check("RETURN") || check("LAMBDA");
    }

    /*
    * Rule 32: statement
    *     stat
    *     ement: expression SEMI
             | RETURN primary SEMI
     */
    public Lexeme statement() throws Exception {
        try {
            Lexeme tree = new Lexeme("statement");
            if (check("RETURN")) {
                tree.left = match("RETURN");
                if (primaryPending()) {
                    tree.left.left = primary();
                } else {
                    tree.left.left = new Lexeme("NULL");
                }
                tree.left.right = match("SEMI");
                return tree;
            } else if (ifExpressionPending()) {
                tree.left = ifChain();
                return tree;
            } else if (expressionPending()) {
                tree.left = expression();
                //System.out.println("<start>");
                //inOrderTraversal(tree);
                //System.out.println("</end>");
                tree.left.right = match("SEMI");
                return tree;
            } else if (whilePending()) {
                tree.left = whileLoop();
                return tree;
            } else if (forPending()) {
                tree.left = forExpression();
                return tree;
            } else if (printPending()) {
                tree.left = printCall();
                match("SEMI");
                return tree;
            } else if (check("DEF")) {
                return functionDef();
            }
        } catch (Exception e) {
            System.out.println("Error: invalid statement");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 29: paramDecList
    *     paramDecList: nullf
                | paramDec
                | paramDecList
     */
    public Lexeme paramDecList() throws Exception {
        try {
            Lexeme tree = new Lexeme("paramDecList");
            if (paramDecPending()) {
                tree.left = paramDec();
                tree.right = paramDecList();
                return tree;
            }
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid function parameters.");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 24: functionDef
    * functionDef: DEF VAR OPAREN paramDecList CPAREN body
     */
    public Lexeme functionDef() throws Exception {
        try {
            Lexeme tree = new Lexeme("functionDef");
            match("DEF");
            tree.left = match("VAR");
            match("OPAREN");
            tree.left.left = paramDecList();
            match("CPAREN");
            tree.left.right = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid function definition");
            System.exit(0);
        }
        return null;
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
        try {
            Lexeme tree = new Lexeme("conditional");
            if (primaryPending()) {
                tree.left = primary();
                Lexeme temp = tree.left;
                if (check("COMPARATOR")) {
                    tree.left = match("COMPARATOR");
                    tree.left.left = temp;
                    Lexeme newTemp = primary();
                    tree.left.right = newTemp;
                } else if (tree.left.type.contentEquals("conditional")) {
                    return tree.left;
                }
                return tree;
            } else if (check("NOT")) {
                tree.left = match("NOT");
                match("OPAREN");
                tree.left.left = conditional();
                match("CPAREN");
                return tree;
            }
        } catch (Exception e) {
            System.out.println("Error: invalid conditional syntax");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 11: ifExpression
    * ifExpression: IF OPAREN conditional CPAREN body
     */
    public Lexeme ifExpression() throws Exception {
        try {
            //System.out.println("<ifExpression>");
            Lexeme tree = match("IF");
            tree.left = match("OPAREN");
            //tree.left.left = conditionalList();
            tree.left.left = conditional();
            match("CPAREN");
            tree.left.right = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid if statement.");
            System.exit(0);
        }
        return null;
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
        try {
            Lexeme tree = match("ELIF");
            tree.left = match("OPAREN");
            tree.left.left = conditional();
            match("CPAREN");
            tree.left.right = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid elif statement");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 12: ifChain
    * ifChain: ifExpression
       | if Expression elifChain
     */
    public Lexeme ifChain() throws Exception {
        try {
            Lexeme tree = ifExpression();
            tree.right = elifChain();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid if chain.");
            System.exit(0);
        }
        return null;
    }

    public boolean elseExpressionPending() {
        return check("ELSE");
    }

    /*
    * Rule 33: else expression
    * elseExpression: ELSE body
     */
    public Lexeme elseExpression() throws Exception {
        try {
            Lexeme tree = match("ELSE");
            tree.left = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid else statement");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 13: Elif Chain
    * elifChain: ELSE body
         | elifExpression
         | elifExpression elifChain
     */
    public Lexeme elifChain() throws Exception {
        try {
            Lexeme tree = new Lexeme("elifChain");
            if (elseExpressionPending()) {
                tree.left = elseExpression();
                return tree;
            } else if (elifExpressionPending()) {
                tree.left = new Lexeme("join");
                tree.left.left = elifExpression();
                tree.left.right = elifChain();
                return tree;
            }
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid elif chain");
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
        try {
            Lexeme tree;
            tree = match("WHILE");
            tree.left = match("OPAREN");
            tree.left.left = conditional();
            match("CPAREN");
            tree.left.right = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid while loop.");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 21: for loop
    * for: FOR OPAREN vardef COMMA conditional COMMA expression CPAREN body
     */
    public Lexeme forExpression() throws Exception {
        try {
            //    System.out.println("in for expression");
            Lexeme tree = new Lexeme("forLoop");
            tree.left = match("FOR");
            tree.left.left = match("OPAREN");
            tree.left.left.left = variableDef();
            tree.left.left.right = match("COMMA");
            tree.left.left.right.left = conditional();
            tree.left.right = match("COMMA");
            tree.left.right.left = expression();
            tree.left.right.right = match("CPAREN");
            tree.left.right.right.left = body();
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid for loop");
            System.exit(0);
        }
        return null;
    }

    public boolean printPending() {
        return check("PRINT");
    }

    /*
    * Rule 34: print call
    * printCall: PRINT OPAREN primary CPAREN
     */
    public Lexeme printCall() throws Exception {
        try {
            Lexeme tree;
            tree = match("PRINT");
            tree.left = match("OPAREN");
            tree.left.left = expression();
            tree.left.right = match("CPAREN");
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid print statement");
            System.exit(0);
        }
        return null;
    }

    /*
    * Rule 35: conditional list
    *     conditionalList: conditional
                   | conditional AND conditional
                   | conditional OR conditional
     */
    public Lexeme conditionalList() throws Exception {
        try {
            Lexeme tree = new Lexeme("conditionalList");
            tree.left = new Lexeme("join");
            Lexeme temp = conditional();
            if (check("AND")) {
                tree.left.left = match("AND");
                tree.left.left.left = temp;
                tree.left.left.right = conditional();
                tree.left.right = conditionalList();
                return tree;
            } else if (check("OR")) {
                tree.left.left = match("OR");
                tree.left.left.right = conditional();
                tree.left.left.left = temp;
                tree.left.right = conditionalList();
                return tree;
            }
            tree.left = temp;
            return tree;
        } catch (Exception e) {
            System.out.println("Error: invalid conditional list");
            System.exit(0);
        }
        return null;
    }

    public boolean arrayDeclarationPending() {
        return check("ARR");
    }

    /*
    * Rule 36: array declaration
    * arrayDeclaration: ARR OSQUARE CSQUARE VAR
    *                 | ARR OSQUARE CSQUARE VAR EQUAL OSQUARE primaryList CSQUARE
     */
    public Lexeme arrayDeclaration() throws Exception {
        try {
            Lexeme tree = new Lexeme("arrayDec");
            tree.left = match("ARR");
            tree.left.left = match("VAR");
            match("EQUAL");
            match("OSQUARE");
            match("CSQUARE");
            return tree;
        } catch (Exception e) {
            System.out.println("Error: Invalid array declaration");
            System.exit(0);
        }
        return null;
    }

    public Lexeme arrayExpression(Lexeme array_name) throws Exception{
        try {
            Lexeme ohBracket = match("OSQUARE");
            Lexeme index = primary();
            Lexeme cBracket = match("CSQUARE");
            if (check("EQUAL")) {
                match("EQUAL");
                Lexeme tree = new Lexeme("arrIndexAssignment");
                tree.left = ohBracket;
                Lexeme jv = new Lexeme("just_var"); // so that the evaluator will get the variable's value (an array)
                jv.left = array_name;
                tree.left.left = jv;
                Lexeme join = new Lexeme("join");
                join.left = index; // the index
                join.right = primary(); // the value we're assigning
                tree.left.right = join;
                return tree;
            }
            Lexeme tree = new Lexeme("arrIndex");
            Lexeme jv = new Lexeme("just_var"); // so that the evaluator will get the variable's value (an array)
            jv.left = array_name;
            tree.left = ohBracket;
            tree.left.left = jv;
            tree.left.right = index;
            Lexeme top = new Lexeme("primary");
            top.left = tree;
            return top;
        } catch (Exception e) {
            System.out.println("Error: Invalid array expression");
            System.exit(0);
        }
        return null;
    }

    public void inOrderTraversal(Lexeme tree) {
        if (tree == null) {
            return;
        }

        System.out.println(tree.type);
        inOrderTraversal(tree.left);
        inOrderTraversal(tree.right);
    }

}