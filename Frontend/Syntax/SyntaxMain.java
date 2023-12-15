package Frontend.Syntax;

import Frontend.Semantic.Error.Error;
import Frontend.Semantic.Error.ErrorType;
import Frontend.Semantic.Symbols.ArraySymbol;
import Frontend.Semantic.Symbols.FuncSymbol;
import Frontend.Semantic.Symbols.Symbol;
import Frontend.Semantic.Symbols.SymbolTable;
import Frontend.Lexical.TokenType;
import Frontend.Lexical.Word;
import Frontend.Syntax.Node.CompUnit;
import Frontend.Syntax.Node.non_Terminal;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static Config.Config.*;

public class SyntaxMain {
    //Frontend.Syntax
    private static ArrayList<Word> tokenList;
    private static int index = 0;   //指向第一个未读的词

    public static Word cur;
    private final CompUnit compUnit = new CompUnit();
    public static void setTokenList(ArrayList<Word> tokenList) {
        SyntaxMain.tokenList = tokenList;
    }

    public static int getIndex() {
        return index;
    }

    public static Word getword(int i) {
        if(i<tokenList.size())
            return tokenList.get(i);
        return new Word("",TokenType.INTTK,0);
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
    public static Word getBefore() {
        if(index>1) {
            return tokenList.get(index-2);
        }
        else {
            return new Word("", TokenType.INTTK, 0);
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
        compUnit.analyse();
    }
    public void print() {
        compUnit.print();
    }
    public non_Terminal getAST() {
        return compUnit;
    }
    public void printAST() {
        int step = 0;
        compUnit.printChild(step);
    }


    //Frontend.Semantic.Error
    private static final ArrayList<Error> errors = new ArrayList<>();
    public static void addError(ErrorType error) {
        errors.add(new Error(error, getBefore().getLine()));
    }

    public static void addError(ErrorType error, int line) {
        errors.add(new Error(error, line));
    }
    public void printError() throws FileNotFoundException{
        PrintWriter writer = new PrintWriter(ErrorPath);
        for (Error error : errors) {
            writer.println(error.toString());
            System.out.println(error.toString());
        }
        writer.close();
    }
    public static boolean isErrorEmpty() {
        return errors.isEmpty();
    }
    public static boolean needReturn = false;   //是否需要return
    public static int forFlag = 0;    //是否在for语句内,1表示1层，2表示2层for
    public static boolean callFlag = false;    //是否是函数调用


    //Symbol
    public static int SymbolTableCnt = 0;
    private static final SymbolTable globalSymbolTable = new SymbolTable(null);
    public static SymbolTable curTable = globalSymbolTable;
    public static void addSymbol(Symbol symbol) {
        curTable.getSymbols().add(symbol);
    }
    public static void addSymbolTable(SymbolTable symbolTable) {
        curTable.getNext().add(symbolTable);
        curTable = symbolTable;
    }
    public static boolean queryCurSymbol(String str) {
        for(Symbol symbol: curTable.getSymbols())
        {
            if(symbol.getName().equals(str))
                return true;
        }
        return false;
    }

    public static boolean queryGlobalSymbol(String str) {
        SymbolTable original = curTable;
        while(curTable != null)
        {
            for(Symbol symbol: curTable.getSymbols())
            {
                if(symbol.getName().equals(str)) {
                    curTable = original;
                    return true;
                }

            }
            curTable = curTable.getPre();
        }
        curTable = original;
        return false;
    }

    public static boolean isConstSymbol(String str) {
        SymbolTable original = curTable;
        while(curTable != null)
        {
            for(Symbol symbol: curTable.getSymbols())
            {
                if(symbol.getName().equals(str) && ((ArraySymbol)symbol).isConst()) {
                    curTable = original;
                    return true;
                }
                else if(symbol.getName().equals(str) && !((ArraySymbol)symbol).isConst()) {
                    curTable = original;
                    return false;
                }

            }
            curTable = curTable.getPre();
        }
        curTable = original;
        return false;
    }

    public static Symbol getSymbol(String str) {
        SymbolTable original = curTable;
        while(curTable != null)
        {
            for(Symbol symbol: curTable.getSymbols())
            {
                if(symbol.getName().equals(str)) {
                    curTable = original;
                    return symbol;
                }

            }
            curTable = curTable.getPre();
        }
        curTable = original;
        return null;
    }

    public static int getFParamCnt(String str) {
        for(Symbol symbol: globalSymbolTable.getSymbols())
        {
            if(symbol.getName().equals(str) && symbol instanceof FuncSymbol)
                return ((FuncSymbol)symbol).getParamCnt();
        }
        return -1;
    }

    public static ArrayList<Integer> getFParamDimension(String str) {
        for(Symbol symbol: globalSymbolTable.getSymbols())
        {
            if(symbol.getName().equals(str) && symbol instanceof FuncSymbol)
                return ((FuncSymbol)symbol).getFuncParams();
        }
        return new ArrayList<>();
    }

    public static void printSymbolTable() {
        globalSymbolTable.print(0);
    }
}
