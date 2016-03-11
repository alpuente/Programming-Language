/**
 * Created by appleowner on 2/12/16.
 */
public class Lexeme {
    protected String type;
    protected String sValue;
    protected int iValue;
    protected double dValue;
    protected boolean bValue;

    /*
    * constructor for a string lexeme
     */
    public Lexeme(String type, String data) {
        this.type = type;
        this.sValue = data;
    }

    /*
    * constructor for an integer lexeme
     */
    public Lexeme(String type, int iData) {
        this.type = type;
        this.iValue = iData;
    }

    public Lexeme(String type, double dData) {
        this.type = type;
        this.dValue = dData;
    }

    /*
    * constructor for a boolean lexeme
     */
    public Lexeme(String type, boolean bData) {
        this.type = type;
        this.bValue = bData;
    }

    /*
    * constructor for a word lexeme
     */
    public Lexeme(String type) {
        this.type = type;
    }



}
