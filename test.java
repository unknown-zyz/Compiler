import Lexical.TokenType;
import Lexical.Word;

import static Syntax.Node.non_Terminal.isFormatString;


public class test{
    public static void main(String[] args) {
        System.out.println(isFormatString(new Word("\"%d'\"", TokenType.INTTK, 1)));
    }
}