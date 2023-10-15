package Syntax.Node;

import static Syntax.SyntaxMain.*;
import Error.ErrorType;
import Symbols.FuncSymbol;
import Symbols.Functype;
import Symbols.SymbolTable;

public class FuncDef extends non_Terminal {
    public static FuncSymbol curFunc;

    public static boolean isFuncReturn ;
    @Override
    public void analyse()
    {
        curFunc = null;
        isFuncReturn = false;
        if(cur_equal("void")||cur_equal("int"))
        {
            boolean isVoid = cur_equal("void");
            needReturn = cur_equal("int");
            add_analyse(new FuncType());
            if(isIdent(cur))
            {
                String name = cur.getToken();
                if(!queryCurSymbol(name))
                {
                    FuncSymbol funcSymbol = new FuncSymbol(name, isVoid ? Functype.VOID : Functype.INT);
                    curFunc = funcSymbol;
                    addSymbol(funcSymbol);
                    //新作用域
                    addSymbolTable(new SymbolTable(curTable));
                }
                else
                    addError(ErrorType.b);
                addChild(new Ident(cur));
                next();
                if(cur_equal("(")) {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("int"))
                        add_analyse(new FuncFParams());
                    if(cur_equal(")"))
                    {
                        addChild(new Symbol(cur));
                        next();
                    }
                    else
                        addError(ErrorType.j);
                    add_analyse(new Block());
                    if(needReturn && !isFuncReturn)
                        addError(ErrorType.g);
                    needReturn = false;
                }
            }
        }
    }
}
