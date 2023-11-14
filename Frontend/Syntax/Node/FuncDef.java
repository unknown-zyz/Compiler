package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.*;
import Frontend.Semantic.Error.ErrorType;
import Frontend.Lexical.TokenType;
import Frontend.Lexical.Word;
import Frontend.Semantic.Symbols.FuncSymbol;
import Frontend.Semantic.Symbols.Functype;
import Frontend.Semantic.Symbols.SymbolTable;

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
                    Block block = new Block();
                    add_analyse(block);
                    //在void函数结尾添加 return;
                    Reserved reserved = new Reserved(new Word("return", TokenType.RETURNTK, 0));
                    Symbol symbol = new Symbol(new Word(";", TokenType.SEMICN, 0));
                    Stmt stmt = new Stmt();
                    stmt.addChild(reserved);
                    stmt.addChild(symbol);
                    ASTNode node = block.removeChild(); // 取出'}'
                    block.addChild(stmt);
                    block.addChild(node);
                    if(needReturn && !isFuncReturn)
                        addError(ErrorType.g);
                    needReturn = false;
                }
            }
        }
    }
}
