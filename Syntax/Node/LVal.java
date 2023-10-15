package Syntax.Node;

import static Syntax.SyntaxMain.*;
import Error.ErrorType;

public class LVal extends non_Terminal {
    @Override
    public void analyse()
    {
        if(!queryGlobalSymbol(cur.getToken()))
            addError(ErrorType.c, cur.getLine());
        addChild(new Ident(cur));
        next();
        while (cur_equal("["))
        {
            addChild(new Symbol(cur));
            next();
            add_analyse(new Exp());
            if (cur_equal("]"))
            {
                addChild(new Symbol(cur));
                next();
            }
            else
                addError(ErrorType.k);
        }
    }
}
