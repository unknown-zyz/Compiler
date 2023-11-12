package Syntax.Node;

import static Syntax.SyntaxMain.*;
import Error.ErrorType;
import Symbols.ArraySymbol;

public class VarDef extends non_Terminal {
    @Override
    public void analyse() {
        if(isIdent(cur))
        {
            int dimension = 0;
            String name = cur.getToken();
            Ident ident = new Ident(cur);
            addChild(ident);
            next();
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
            if(!queryCurSymbol(name))
            {
                addSymbol(new ArraySymbol(name,false,dimension));
                ident.setDim(dimension);
            }
            else
                addError(ErrorType.b);
            if(cur_equal("="))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new InitVal());
            }
        }
    }
}
