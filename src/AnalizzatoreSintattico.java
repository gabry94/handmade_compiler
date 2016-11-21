import java.util.ArrayList;

/**
 * Created by gabri on 10/11/2016.
 */
public class AnalizzatoreSintattico {

    private AnalizzatoreLessicale lexer;
    private Token token;
    private ArrayList<Token> tokens = new ArrayList<>();
    private int index;


    public AnalizzatoreSintattico(AnalizzatoreLessicale lexer) {
        this.lexer = lexer;
    }

/*
Grammar:

    Program -> Stat Program1
    Program1 -> SEMI Stat Program1 | epsilon

    Stat -> matched_stat | open_stat

    matched_stat -> IF Id_num Relaz Id_num THEN matched_stat  ELSE matched_stat | ID = ID_num
    open_stat -> IF Id_num Relaz Id_num THEN open_stat1
    opens_stat1 -> stat | matched_stat ELSE open_stat

    Id_num -> ID | NUM
    Relaz -> LE | NE | LT | GE | GT
 */

    public void reset() {
        index = 0;
        tokens.clear();
    }

    public void backtrack(int index) {
        this.index = index;
        goOn();
    }

    public boolean analyze() {

        while (program()) {

            reset();

            if (lexer.isFinished())
                return true;
        }

        //Debug Information
        int i = 0;
        for (Token t : tokens)
            System.out.println(++i + ") " + t);

        return false;
    }

    private void goOn() {
        if (lexer.isFinished() && index == tokens.size())
            return;

        if (index == tokens.size() || tokens.size() == 0) {
            token = lexer.nextToken();
            tokens.add(token);
            index++;
        } else {
            token = tokens.get(index++);
        }
    }

    public boolean program() {

        goOn();

        if (!stat())
            return false;
        goOn();
        if (!program1())
            return false;

        return true;
    }

    public boolean program1() {


        if (!token.getClasse().equals("SEMI")) {
            return lexer.isFinished();
        }

        goOn();

        if (!stat())
            return false;

        goOn();

        if (!program1())
            return false;

        return true;
    }


    public boolean stat() {

        int local_index = index - 1; // -1 because I increased the index before method's call

        if (!matched_stat()) {

            backtrack(local_index);

            return open_stat();
        }

        return true;
    }

    public boolean matched_stat() {

        if (token.getClasse().equals("IF")) {

            goOn();

            if (!token.getClasse().equals(Token.ID_CLASS) && !token.getClasse().equals(Token.NUMBER_CLASS))
                return false;

            goOn();

            if (!token.getClasse().equals(Token.RELOP_CLASS))
                return false;

            goOn();

            if (!token.getClasse().equals(Token.ID_CLASS) && !token.getClasse().equals(Token.NUMBER_CLASS))
                return false;

            goOn();

            if (!token.getClasse().equals("THEN"))
                return false;

            goOn();

            if (!matched_stat())
                return false;

            goOn();

            if (!token.getClasse().equals("ELSE"))
                return false;


            goOn();

            if (!matched_stat())
                return false;

        } else {

            if (!token.getClasse().equals(Token.ID_CLASS))
                return false;

            goOn();

            if (!token.getClasse().equals(Token.ASSIGN_CLASS))
                return false;

            goOn();

            if (!token.getClasse().equals(Token.ID_CLASS) && !token.getClasse().equals(Token.NUMBER_CLASS))
                return false;
        }

        return true;
    }

    public boolean open_stat() {
        if (!token.getClasse().equals("IF"))
            return false;

        goOn();

        if (!token.getClasse().equals(Token.ID_CLASS) && !token.getClasse().equals(Token.NUMBER_CLASS))
            return false;

        goOn();

        if (!token.getClasse().equals(Token.RELOP_CLASS))
            return false;

        goOn();

        if (!token.getClasse().equals(Token.ID_CLASS) && !token.getClasse().equals(Token.NUMBER_CLASS))
            return false;

        goOn();

        if (!token.getClasse().equals("THEN"))
            return false;

        goOn();

        if (!open_stat1())
            return false;

        return true;
    }

    public boolean open_stat1() {

        int local_index = index - 1;// -1 because I increased the index before method's call
        if (!stat()) {

            backtrack(local_index);

            if (!matched_stat())
                return false;

            goOn();

            if (!token.getClasse().equals("ELSE"))
                return false;

            goOn();

            if (!open_stat())
                return false;
        }

        return true;
    }

}
