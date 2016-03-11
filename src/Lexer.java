import com.sun.xml.internal.fastinfoset.util.CharArray;
import com.sun.xml.internal.fastinfoset.vocab.ParserVocabulary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.StringReader;

/**
 * Created by appleowner on 2/12/16.
 */
public class Lexer {
    int offset = 0; // keeping track of the index offset in the individual lexeme functions

    public Lexer(String fileContents) {
        //System.out.println("hey");
        ArrayList<Lexeme> lexemes = Lex(fileContents);
        //System.out.println("oh");
        for (int i = 0; i < lexemes.size(); i++) {
            System.out.println(lexemes.get(i).type);
        }
        //System.out.println("hey");
    }

    /*
    * will take in a string (for now) and return a list of lexemes (?)
     */
    public ArrayList<Lexeme> Lex(String fileContents) {
        ArrayList<Lexeme> lexemes = new ArrayList<Lexeme>();
        fileContents = fileContents.trim(); // get rid of leading and trailing whitespace
        char[] characters = fileContents.toCharArray(); // make a char array out of input string
        //System.out.println(characters.length);
        for (int i = 0; i < characters.length; i++) {
            //System.out.println(i);
            char ch = characters[i];
            System.out.println("characters[i] " + characters[i]);
            if (ch == '\"') { // if the character is a quotation, make a string lexeme
                lexemes.add(lexString(characters, i + 1));
                i += offset;
            } else if (isNumeric(ch)) { // if the character is numeric, make a number lexeme (probably double or int)
                lexemes.add(lexNumber(ch, characters, i + 1));
                i += offset;
            } else if (ch == ';') {
                lexemes.add(new Lexeme("semi"));
            } else if (ch == '(') {
                lexemes.add(new Lexeme("("));
            } else if (ch == ')') {
                lexemes.add(new Lexeme(")"));
            } else if (ch == '{') {
                lexemes.add(new Lexeme("{"));
            } else if (ch == '}') {
                lexemes.add(new Lexeme("}"));
            } else if (ch == '+') {
                lexemes.add(new Lexeme("+"));
            } else if (ch == '/') {
                lexemes.add(new Lexeme("/"));
            } else if (ch == '*' || ch == '+' || ch == '=' || ch =='-') { // if a + or * check if it's single or double (* or **)
                System.out.println("chars[i-1] " + characters[i-1]);
                System.out.println("char " + ch);
                lexemes.add(checkChar(characters, i + 1, ch)); // plus 1 because checkChar also takes the character
                i += offset;
            } else if (ch == ' ') { // just keep moving if ch is a space

            } else if (isAlpha(ch)) { // if an alpha character
                //System.out.println(ch)
                lexemes.add(lexAlpha(characters, i)); // make a "word" lexeme
                i += offset;
            } else {
                // woah bad character
            }
        }

        return lexemes;
    }

    public Lexeme lexAlpha(char[] chars, int index) {
        int i = index;
        char ch = chars[index];
        String buffer = "";
        while (ch != ' ' && i <= chars.length && (isNumeric(ch) || isAlpha(ch))) { // check how much of the char array got parsed, subtract 1 to account for increment
            //System.out.println("ch " + ch + " i " + i);
            buffer += ch;
            i = i + 1;
            ch = chars[i];
        }
        //System.out.println("buffer " + buffer.length() + )
        offset = buffer.length()-1;
        return new Lexeme(buffer);
    }

    /*
    * a function to check for duplicate chars, to use with the ++ and ** operators
     */
    public Lexeme checkChar(char[] chars, int index, char ch) {
        int i = index;
        System.out.println("ch " + ch + " ch i + 1 " + chars[i]);
        if (chars[i] == ch) { // if the next char is the same as the first, return a lexeme with 2 chars as the type
            offset = 1;
            return new Lexeme("" + ch + ch);
        } else if (chars[i] == ' ') {
            offset = 0;
            return new Lexeme("" + ch);
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
        return new Lexeme("string", buffer);
    }

    /*
    * function to return a numeric lexeme, either an int or a double
    * should return error if a character is
     */
    public Lexeme lexNumber(char ch, char[] chars, int index) {
        int i = index;
        String buffer = "" + ch; // don't forget the first digit!
        ch = chars[i];
        boolean isDouble = false;
        while (i != ' ' && i <= chars.length) { // keep going until there's a space
            //System.out.println("ch " + ch);
            //System.out.println("i " + i + "chars.length " + chars.length);
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
        //System.out.println("buffer l " + buffer.length() + "offset " + offset);
        if (isDouble) {
            return new Lexeme("double", buffer);
        } else {
            return new Lexeme("integer", buffer);
        }
    }

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("input.txt"));
            String contents = "";
            String line;
            while((line = br.readLine()) != null) {
                contents += line;
            }

            Lexer lexer = new Lexer(contents);

        } catch (IOException e) {

        }
    }
}
