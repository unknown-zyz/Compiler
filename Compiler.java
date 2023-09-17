import Lexical.Lexer;
import Lexical.Word;

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
            Lexer.analyse(article.toString());
            ArrayList<Word> tokenList = Lexer.getTokenList();
            FileWriter fw = new FileWriter("output.txt");
            for (Word word : tokenList) {
                fw.write(word.getType() + " " + word.getToken()+"\n");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
