import Lexical.TokenType;
import Lexical.Word;

import java.util.ArrayList;

import static Syntax.Node.non_Terminal.isFormatString;


public class test{
    public static void main(String[] args) {
        System.out.println(isFormatString(new Word("\"%d'\"", TokenType.INTTK, 1)));
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
    }
}