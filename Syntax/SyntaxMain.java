package Syntax;

import Lexical.Word;
import Syntax.Node.CompUnit;

import java.util.ArrayList;

public class SyntaxMain {
    private static ArrayList<Word> tokenList;
    private static int index = 0;   //指向第一个未读的词
    public static Word cur;
    CompUnit compUnit = new CompUnit();

    public static void setTokenList(ArrayList<Word> tokenList) {
        SyntaxMain.tokenList = tokenList;
    }

    public static void next() {
        cur = tokenList.get(index);
        index++;
    }

    public void analyse() {
        next();
        compUnit.analyse();
    }

    public void printAST() {
        compUnit.print();
    }

}
