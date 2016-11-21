/**
 * Created by gabri on 10/11/2016.
 */
public class Token {

    private String classe, lessema;

    public static final String ID_CLASS = "ID",
            NUMBER_CLASS = "NUMBER",
            RELOP_CLASS = "RELOP",
            SEMI_CLASS = "SEMI",
            ASSIGN_CLASS = "ASSIGN";

    public Token(String classe, String lessema) {
        this.classe = classe;
        this.lessema = lessema;
    }

    public Token(String classe) {
        this(classe, null);
    }


    public String getClasse() {
        return classe;
    }

    public String getLessema() {
        return lessema;
    }

    public void setLessema(String lessema) {
        this.lessema = lessema;
    }

    @Override
    public String toString() {

        return ("Token(" + classe) + (lessema == null ? "" : "," + lessema) + ")";
    }
}
