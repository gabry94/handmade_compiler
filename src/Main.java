import java.io.IOException;

/**
 * Created by gabri on 10/11/2016.
 */
public class Main {

    public static void main(String[] args) throws IOException {


        AnalizzatoreLessicale lex = new AnalizzatoreLessicale("inputFile2");

        AnalizzatoreSintattico sintattico = new AnalizzatoreSintattico(lex);
        System.out.println(sintattico.analyze());



    }
}
