package Syntax;

import Lexical.TokenType;
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

    public static boolean isLVal() {
        int i = index, flag = 0;
        for(;i<tokenList.size() && flag==0; i++)
        {
            if(tokenList.get(i).getToken().equals("="))    return true;
            if(tokenList.get(i).getToken().equals(";"))    flag=1;
        }
        return false;
    }

    public static void next() {
        if(index<tokenList.size()) {
            cur = tokenList.get(index);
            index++;
        }
    }

    public static Word getNext() {
        if(index<tokenList.size()) {
            return tokenList.get(index);
        }
        else {
            return new Word("", TokenType.INTTK, 0);
        }
    }

    public static Word getNextNext() {
        if(index + 1<tokenList.size()) {
            return tokenList.get(index+1);
        }
        else {
            return new Word("", TokenType.INTTK, 0);
        }
    }

    public void analyse() {
        next();
        compUnit.analyse();}

    public void printAST() {
        compUnit.print();
    }

//    public void printChild() {
//        lOrExp.printChild();
//    }

}
