package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.*;
import Frontend.Semantic.Error.ErrorType;

public class LVal extends non_Terminal {
    @Override
    public void analyse()
    {
        if(!queryGlobalSymbol(cur.getToken()))
            addError(ErrorType.c, cur.getLine());
        int dim = 0;
        Ident ident = new Ident(cur);
        addChild(ident);
        next();
        while (cur_equal("["))
        {
            dim++;
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
        ident.setDim(dim);
    }
}
