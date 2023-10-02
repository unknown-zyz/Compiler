import Lexical.TokenType;
import Lexical.Word;
import Syntax.Node.AddExp;

import java.io.FileWriter;
import java.io.IOException;

import static Syntax.Node.non_Terminal.isFormatString;
import static Syntax.Node.non_Terminal.isIdent;
import static Syntax.SyntaxMain.next;

public class test{
    public static void main(String[] args) {
        System.out.println(isFormatString(new Word("\"A%d\"", TokenType.INTTK, 1)));
        if((true||false)&&false)
            System.out.println("1");
    }
}