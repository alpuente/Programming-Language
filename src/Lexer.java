import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Map;

/**
 * Created by appleowner on 2/12/16.
 */
public class Lexer {
    int offset = 0; // keeping track of the index offset in the individual lexeme functions
    char[] characters; // input character array
    int currentIndex = 0; // current index into the array of input characters
    Hashtable keywords;

    public Lexer(String fileContents) {
        //System.out.println(fileContents);
        characters = fileContents.toCharArray();
        initiateKeyWords();
    }


    /*
     make a hashmap to hold all of the keywords for the language
     */
    public void initiateKeyWords() {
        keywords = new Hashtable();
        keywords.put("for" , "FOR");
        keywords.put("var", "VARDEF");
        keywords.put("if", "IF");
        keywords.put("elif", "ELIF");
        keywords.put("while", "WHILE");
        keywords.put("def", "DEF");
        keywords.put("return", "RETURN");
        keywords.put("or", "OR");
        keywords.put("and", "AND");
        keywords.put("new", "NEW");
        keywords.put("print", "PRINT");
        keywords.put("lambda", "LAMBDA");
        keywords.put("true", "BOOLEAN");
        keywords.put("false", "BOOLEAN");
        keywords.put("null", "NULL");
        keywords.put("arr", "ARR");
        //keywords.put();
    }

    public Lexeme lex() {
        char ch = characters[currentIndex];
        if (ch == '\"') { // if the character is a quotation, make a string lexeme
            //currentIndex += offset + 1;
            return lexString(characters, currentIndex + 1);
        } else if (isNumeric(ch)) { // if the character is numeric, make a number lexeme (probably double or int)
            //currentIndex += offset + 1;
            return lexNumber(ch, characters, currentIndex + 1);
        } else if (ch == ';') {
            currentIndex += 1;
            return new Lexeme("SEMI");
        } else if (ch == ',') {
            currentIndex += 1;
            return new Lexeme("COMMA");
        } else if (ch == '(') {
            currentIndex += 1;
            return new Lexeme("OPAREN");
        } else if (ch == ')') {
            currentIndex += 1;
            return new Lexeme("CPAREN");
        } else if (ch == '{') {
            currentIndex += 1;
            return new Lexeme("OBRACKET");
        } else if (ch == '}') {
            currentIndex += 1;
            return new Lexeme("CBRACKET");
        } else if (ch == '[') {
            currentIndex += 1;
            return new Lexeme("OSQUARE");
        } else if (ch == ']') {
            currentIndex += 1;
            return new Lexeme("CSQUARE");
        } else if (ch == '~') {
            return new Lexeme("EOF"); // arbitrary end of file character
        } else if (ch == ':') {
            if (characters[currentIndex + 1] == ')') {
                currentIndex += 1;
                return getComment();
            }
        } else if (ch == '%' || ch == '/') {
            currentIndex += 1;
            return new Lexeme("BINOPERATOR", "" + ch);
        }
        else if (ch == '<') {
            if (characters[currentIndex+1] == '=') {
                currentIndex += 2;
                return new Lexeme("COMPARATOR","<=");
            } else {
                currentIndex += 1;
                return new Lexeme("COMPARATOR", "<");
            }
        } else if (ch == '>') {
            currentIndex += 1;
            return new Lexeme("COMPARATOR", ">");
        } else if (ch == '^') {
            currentIndex += 1;
            return new Lexeme("BINOPERATOR", ch);
        } else if (ch == '=') {
            if (characters[currentIndex + 1] == '>' || characters[currentIndex + 1] == '=') {
                currentIndex += 2;
                return new Lexeme("COMPARATOR", "=" + characters[currentIndex + 1]);
            } else {
                currentIndex += 1;
                return new Lexeme("EQUAL");
            }
        }
        else if (ch == '*' || ch == '+' || ch =='-') { // if a + or * check if it's single or double (* or **)
            return checkChar(characters, currentIndex + 1, ch); // plus 1 because checkChar also takes the character
        } else if (ch == ' ') { // just keep moving if ch is a space
            currentIndex += 1;
            return new Lexeme("SPACE");
        } else if (isAlpha(ch)) { // if an alpha character
            return lexAlpha(characters, currentIndex); // make a "word" lexeme
        }

        return null;
    }

    public Lexeme lexAlpha(char[] chars, int index) {
        int i = index;
        char ch = chars[index];
        String buffer = "";
        while (ch != ' ' && i <= chars.length && (isNumeric(ch) || isAlpha(ch))) { // check how much of the char array got parsed, subtract 1 to account for increment
            buffer += ch;
            if (keywords.containsKey(buffer)) {
                offset = buffer.length() - 1;
                currentIndex += offset + 1;
                if (keywords.get(buffer).toString() == "VARDEF") {
                    return new Lexeme(keywords.get(buffer).toString(), buffer);
                }
                return new Lexeme(keywords.get(buffer).toString()); // make a new buffer with the keyword
            }
            i = i + 1;
            ch = chars[i];
        }
        //System.out.println("buffer " + buffer);
        offset = buffer.length() - 1;
        currentIndex += offset + 1;
        return new Lexeme("VAR", buffer);
    }

    /*
    * a function to check for duplicate chars, to use with the ++ and ** operators
     */
    public Lexeme checkChar(char[] chars, int index, char ch) {
        int i = index;
        if (chars[i] == ch) { // if the next char is the same as the first, return a lexeme with 2 chars as the type
            offset = 1;
            currentIndex += offset + 1;
            return new Lexeme("UNIOPERATOR", "" + ch + ch);
        } else if (chars[i] == ' ') {
            offset = 0;
            currentIndex += 1;
            return new Lexeme("BINOPERATOR", "" + ch);
        } else {
            return new Lexeme("BAD CHAR"); // THROW AN ERROR
        }
    }

    /*
    * check if a character is alpha, based on its ASCII value
     */
    public boolean isAlpha(char ch) {
        //System.out.println(((int) ch >= 65 && (int) ch <=122));
        return ((int) ch >= 65 && (int) ch <=122);
    }

    /*
    gets a comment
     */
    public Lexeme getComment() {
        String buffer = "";
/*        for (int i = 0; i < characters.length; i++) {
            System.out.print(characters[i]);
        }*/
        //currentIndex += 1;
        while (!((characters[currentIndex] == '(') && (characters[currentIndex + 1] == ':'))) {
            buffer += characters[currentIndex];
            System.out.println("buffer " + buffer);
            currentIndex = currentIndex + 1;
        }

        offset = buffer.length();

        currentIndex += buffer.length() + 2;

        return new Lexeme("COMMENT", buffer);
    }

    /*
    * check if a character is numeric, based on its ASCII value
     */
    public boolean isNumeric(char ch) {
        return (int) ch >= 48 && (int) ch <= 57;
    }

    /*
    * function to return a string lexeme
    * should return an error if there is no end quotation
     */
    public Lexeme lexString(char[] chars, int index) {
        String buffer = ""; // make a string buffer to store each char
        int i = index;
        char ch = chars[i];
        while (ch != '\"') {
            buffer += ch; // add the character to buffer
            i = i + 1;
            ch = chars[i]; // move to the next character
        }
        offset = buffer.length() - 1; // check how much of the char array got parsed, subtract 1 to account for incrementation in for loop
        currentIndex += offset + 3;
        return new Lexeme("STRING", buffer);
    }

    /*
    * function to return a numeric lexeme, either an int or a double
    * should return error if a character is
     */
    public Lexeme lexNumber(char ch, char[] chars, int index) {
        //System.out.println("call");
        int i = index;
        String buffer = "" + ch; // don't forget the first digit!
        //System.out.println("buffer " + ch);
        ch = chars[i];
        boolean isDouble = false;
        //System.out.println("i != ' ' && i <= chars.length " + (ch != ' ' && i <= chars.length));
        while (ch != ' ' && i <= chars.length) { // keep going until there's a space
            //System.out.println("ch " + ch);
            if (ch == '.') {
                isDouble = true; // if there's a decimal, then it's a double
                buffer += ch; // add the decimal to buffer
            } else if (isNumeric(ch)) {
                buffer += ch; // add the digit to buffer
            } else {
                break;
            }
            i = i + 1;
            ch = chars[i]; // move to the next character
        }

        offset = buffer.length() - 1; // check how much of the char array got parsed, subtract 1 to account for incre
        currentIndex += offset + 1;
        //System.out.println("buffer " + buffer);
        if (isDouble) {
            return new Lexeme("DOUBLE", Double.parseDouble(buffer));
        } else {
            return new Lexeme("INTEGER", Integer.parseInt(buffer));
        }
    }

}
