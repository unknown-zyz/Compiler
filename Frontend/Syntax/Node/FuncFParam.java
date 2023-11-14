package Frontend.Syntax.Node;

import static Frontend.Syntax.Node.FuncDef.curFunc;
import static Frontend.Syntax.SyntaxMain.*;
import Frontend.Semantic.Error.ErrorType;
import Frontend.Semantic.Symbols.ArraySymbol;
import Frontend.Semantic.Symbols.FuncParam;

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
                Ident ident = new Ident(cur);
                addChild(ident);
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
                        ident.setDim(dimension);
                    }
                    else
                        addError(ErrorType.b);
                }

            }
        }
    }
}
