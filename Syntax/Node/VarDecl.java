package Syntax.Node;

import static Syntax.SyntaxMain.*;
import Error.ErrorType;

public class VarDecl extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("int"))
        {
            addChild(new Reserved(cur));
            next();
            add_analyse(new VarDef());
            while(cur_equal(","))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new VarDef());
            }
            if(cur_equal(";")) {
                addChild(new Symbol(cur));
                next();
            }
            else
                addError(ErrorType.i);
        }
    }
}
