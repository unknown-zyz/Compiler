package Syntax.Node;

import static Syntax.Node.FuncDef.curFunc;
import static Syntax.SyntaxMain.*;
import Error.ErrorType;
import Symbols.ArraySymbol;
import Symbols.FuncParam;

public class FuncFParam extends non_Terminal {
    @Override
    public void analyse()
    {
        int dimension = 0;
        if(cur_equal("int"))
        {
            addChild(new Reserved(cur));
            next();
            if(isIdent(cur))
            {
                String name = cur.getToken();
                addChild(new Ident(cur));
                next();
                if(cur_equal("[")) {
                    dimension++;
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("]"))
                    {
                        addChild(new Symbol(cur));
                        next();
                    }
                    else
                        addError(ErrorType.k);

                    while(cur_equal("["))
                    {
                        dimension++;
                        addChild(new Symbol(cur));
                        next();
                        add_analyse(new ConstExp());
                        if(cur_equal("]"))
                        {
                            addChild(new Symbol(cur));
                            next();
                        }
                        else
                            addError(ErrorType.k);
                    }
                }
                if(curFunc != null)
                {
                    if(!queryCurSymbol(name))
                    {
                        addSymbol(new ArraySymbol(name,false,dimension));
                        curFunc.addFuncParam(new FuncParam(name, dimension));
                    }
                    else
                        addError(ErrorType.b);
                }

            }
        }
    }
}
