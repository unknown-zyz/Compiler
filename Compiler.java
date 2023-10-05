import Lexical.Lexer;
import Lexical.Word;
import Syntax.SyntaxMain;

import java.io.*;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("testfile.txt"));
            String str = br.readLine();
            StringBuilder article = new StringBuilder();
            while(str != null) {
                article.append(str);
                article.append('\n');
                str = br.readLine();
            }
            Lexer lexer = new Lexer();
            lexer.analyse(article.toString());
            ArrayList<Word> tokenList = lexer.getTokenList();
//            FileWriter fw = new FileWriter("output.txt");
//            for (Word word : tokenList) {
//                fw.write(word.getType() + " " + word.getToken()+"\n");
//            }
//            fw.close();

            SyntaxMain.setTokenList(tokenList);
            SyntaxMain syntaxMain = new SyntaxMain();
            syntaxMain.analyse();

            FileOutputStream AST_fileOut = new FileOutputStream("AST.txt");
            PrintStream AST_printOut = new PrintStream(AST_fileOut);
            System.setOut(AST_printOut);
            syntaxMain.printAST();
            AST_printOut.close();
            AST_fileOut.close();

            FileOutputStream fileOut = new FileOutputStream("output.txt");
            PrintStream printOut = new PrintStream(fileOut);
            System.setOut(printOut);
            syntaxMain.print();
            printOut.close();
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
