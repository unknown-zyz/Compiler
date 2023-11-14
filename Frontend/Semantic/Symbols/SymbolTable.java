package Frontend.Semantic.Symbols;

import java.util.ArrayList;
import static Frontend.Syntax.SyntaxMain.SymbolTableCnt;

public class SymbolTable {
    private int id;
    private ArrayList<Symbol> symbols = new ArrayList<>();
    private SymbolTable pre;
    private ArrayList<SymbolTable> next = new ArrayList<>();

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    public SymbolTable getPre() {
        return pre;
    }

    public ArrayList<SymbolTable> getNext() {
        return next;
    }

    public SymbolTable(SymbolTable pre) {
        this.id = ++SymbolTableCnt;
        this.pre = pre;
    }

    public int getId() {
        return id;
    }

    public void print(int step) {
        System.out.println(id+"-----");
        for(Symbol symbol: getSymbols())
        {
            for(int i=0;i<step;i++)
                System.out.print("\t");
            System.out.println(symbol);
        }
        for(SymbolTable symbolTable:getNext())
            symbolTable.print(step+1);
    }
}
