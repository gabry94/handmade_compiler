import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Gabriele Lombari on 15/11/2016.
 */
public class AnalizzatoreLessicale {

    private static String REGEX_DIGITS = "[0-9]+";
    private static String REGEX_LETTER = "[A-Za-z]";
    private static String REGEX_LETTER_OR_DIGITS = REGEX_DIGITS + "|" + REGEX_LETTER;
    private static String REGEX_DELIM = " |\n|\t|\r|;";

    private String code;
    private int current_index, state, exState = 0;

    private String lexem = "";
    private char c;


    private HashMap<String, String> keywordMap;
    private HashMap<String, String> symbolsTable;


    public AnalizzatoreLessicale(String file) throws IOException {
        code =  new String(Files.readAllBytes(Paths.get(file)));
        current_index = 0;
        state = 0;

        keywordMap = new HashMap<>();
        symbolsTable = new HashMap<>();

        keywordMap.put("if", "IF");
        keywordMap.put("else", "ELSE");
        keywordMap.put("then", "THEN");
    }

    public HashMap<String, String> getSymbolsTable() {
        return symbolsTable;
    }

    public boolean isFinished() {
        return current_index == code.length();
    }

    public void reset(){
        lexem = "";
        state = 0;
    }

    public Token nextToken() {
        Token toReturn = null;

        while ((toReturn = getRelOp()) == null){};

        symbolsTable.put(toReturn.getLessema(), toReturn.getClasse());

        reset();
        return toReturn;
    }


    public Token getRelOp() {

        if(!isFinished()) {
            c = code.charAt(current_index++);
            lexem += c;
        } else {
            if(lexem.trim().length() != 0) {
                if (exState == 9) {
                    if (keywordMap.containsKey(lexem.trim()))
                        return new Token(lexem.trim());
                    return new Token(Token.ID_CLASS, lexem);
                } else if (exState == 12)
                    return new Token(Token.NUMBER_CLASS, lexem);
            }
        }
        int index = current_index;

        switch (state) {

            case 0: {
                if (c == '<') state = 1;
                else if (c == '=') state = 5;
                else if (c == '>') state = 6;
                else {
                    state = 9;
                    return getKeyWordsOrID();
                }
                break;
            }

            case 1: {
                if (c == '=') state = 2;
                else if (c == '>') state = 3;
                else {
                    state = 4;
                    return getRelOp();
                }
                break;
            }

            case 2:
                current_index --;
                return new Token(Token.RELOP_CLASS, "LE");

            case 3:
                current_index --;
                return new Token(Token.RELOP_CLASS, "NE");

            case 4:
                current_index -= 2;
                return new Token(Token.RELOP_CLASS, "LT");

            case 5:
                current_index --;
                return new Token(Token.RELOP_CLASS, "EQ");

            case 6: {
                if (c == '=') state = 7;
                else {
                    state = 8;
                    return getRelOp();
                }
                break;
            }

            case 7:
                current_index --;
                return new Token(Token.RELOP_CLASS, "GE");

            case 8:
                current_index -= 2;
                return new Token(Token.RELOP_CLASS, "GT");

            default:
                if (state < 9) {
                    state = 9;
                    current_index = index;
                }

                return getKeyWordsOrID();

        }

        return null;
    }

    public Token getKeyWordsOrID() {

        int index = current_index;
        switch (state) {

            case 9: {

                if (Pattern.matches(REGEX_LETTER, c + "")) { state = 10; exState = 9;}
                else {
                    state = 12;
                    return getNumber();
                }
                break;
            }

            case 10: {
                if (!Pattern.matches(REGEX_LETTER_OR_DIGITS, c + "")) {
                    state = 11;
                    return getKeyWordsOrID();
                }
                break;
            }

            case 11: {
                current_index --;

                if (keywordMap.containsKey(lexem.substring(0, lexem.length() - 1)))
                    return new Token(lexem.substring(0, lexem.length() - 1).toUpperCase());

                return new Token(Token.ID_CLASS, lexem.substring(0, lexem.length() - 1));
            }

            default:
                if (state < 12) {
                    current_index = index;
                    state = 12;
                }
                return getNumber();
        }

        return null;
    }

    public Token getNumber() {
        int index = current_index;
        switch (state) {

            case 12: {


                if (Pattern.matches(REGEX_DIGITS, c + "")){
                    state = 13;
                    exState = 12;
                }
                else {
                    state = 22;
                    return getDelim();
                }
                break;
            }

            case 13: {
                if (c == '.') {
                    state = 14;
                } else if (Pattern.matches(REGEX_DIGITS, c + "")) ;
                else if (c == 'E')
                    state = 16;
                else {
                    state = 22;
                    current_index --;

                    return new Token(Token.NUMBER_CLASS, lexem.substring(0, lexem.length() - 1));
                }
                break;
            }

            case 14: {
                if (Pattern.matches(REGEX_DIGITS, c + ""))
                    state = 15;
                else {
                    state = 22;
                    return getDelim();
                }
                break;
            }

            case 15: {
                if (Pattern.matches(REGEX_DIGITS, c + "")) ;
                else if (c == 'E') state = 16;
                else {
                    return new Token(Token.NUMBER_CLASS, lexem);
                }
                break;
            }

            case 16: {
                if (c == '+' || c == '-') state = 17;
                else if (Pattern.matches(REGEX_DIGITS, c + "")) state = 18;
                else {
                    state = 22;
                    return getDelim();
                }
                break;
            }

            case 17: {
                if (Pattern.matches(REGEX_DIGITS, c + "")) state = 18;
                else {
                    state = 22;
                    return getDelim();
                }
                break;
            }
            case 18: {
                if (Pattern.matches(REGEX_DIGITS, c + "")) ;
                else {
                    state = 21;
                }
                break;
            }

            case 19:
            case 20:
            case 21: {
                return new Token(Token.NUMBER_CLASS, lexem);
            }

            default:
                if (state < 22) {
                    current_index = index;
                    state = 25;
                }
                return getAssign();
        }

        return null;
    }

    public Token getAssign(){

        switch (state){
            case 25:{
                if(c == ':'){
                    if(lexem.trim().length() > 0) {
                        current_index --;
                        if (exState == 9) {
                            if (keywordMap.containsKey(lexem.trim()))
                                return new Token(lexem.trim());
                            return new Token(Token.ID_CLASS, lexem.substring(0, lexem.length()));
                        } else if (exState == 12)
                            return new Token(Token.NUMBER_CLASS, lexem.substring(0, lexem.length()));
                    }
                    state = 26;
                    break;
                }
            }

            case 26:{
                if(c == '='){
                    return new Token(Token.ASSIGN_CLASS);
                }
            }

            default:

                if(state < 22)
                    state = 22;

                getDelim();
        }
        return null;
    }

    public Token getDelim() {
        switch (state) {
            case 22: {
                if (Pattern.matches(REGEX_DELIM, c + "")) {
                    state = 23;

                    if(c == ';' && lexem.length() > 1){
                        if (exState == 9) {
                            if (keywordMap.containsKey(lexem.trim()))
                                return new Token(lexem.trim());
                            return new Token(Token.ID_CLASS, lexem);
                        } else if (exState == 12)
                            return new Token(Token.NUMBER_CLASS, lexem);
                    } else if(c == ';'){
                        reset();
                        return new Token(Token.SEMI_CLASS);
                    }

                    if(lexem.trim().length() == 0)
                        reset();
                }
                else state = 25;
                break;
            }

            case 23: {
                if (!Pattern.matches(REGEX_DELIM, c + "")) {
                    state = 24;
                    return getDelim();
                }
                break;
            }

            case 24: {
                if(lexem.trim().length() != 0) {
                    if (exState == 9) {
                        if (keywordMap.containsKey(lexem.trim()))
                            return new Token(lexem.trim());
                        return new Token(Token.ID_CLASS, lexem);
                    } else if (exState == 12)
                        return new Token(Token.NUMBER_CLASS, lexem);
                    break;
                }

                reset();
            }
            default:
                throw new RuntimeException("Lexical error " + lexem + " " + c);
        }

        return null;
    }

}
