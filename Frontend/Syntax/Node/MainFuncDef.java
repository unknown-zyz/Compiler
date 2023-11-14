package Frontend.Syntax.Node;

import Frontend.Semantic.Symbols.SymbolTable;
import Frontend.Semantic.Error.ErrorType;

import static Frontend.Syntax.SyntaxMain.*;
import static Frontend.Syntax.SyntaxMain.curTable;
import static Frontend.Syntax.Node.FuncDef.isFuncReturn;

public class MainFuncDef extends non_Terminal {
    @Override
    public void analyse()
    {
        isFuncReturn = false;
        needReturn = true;
        if(cur_equal("int"))
        {
            addChild(new Reserved(cur));
            next();
            if(cur_equal("main"))
            {
                addChild(new Reserved(cur));
                next();
                if(cur_equal("("))
                {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal(")"))
                    {
                        addChild(new Symbol(cur));
                        next();
                    }
                    else
                        addError(ErrorType.j);
                    //新作用域
                    addSymbolTable(new SymbolTable(curTable));
                    add_analyse(new Block());
                    if(needReturn && !isFuncReturn)
                        addError(ErrorType.g, cur.getLine());
                    needReturn = false;
                }
            }
        }
    }
}
