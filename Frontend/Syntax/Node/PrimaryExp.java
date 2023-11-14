package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.*;
import Frontend.Semantic.Error.ErrorType;

public class PrimaryExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")) {
            addChild(new Symbol(cur));
            next();
            add_analyse(new Exp());
            if(cur_equal(")")) {
                addChild(new Symbol(cur));
                next();
            }
            else
                addError(ErrorType.j);
        }
        else if(isIdent(cur)) {
            add_analyse(new LVal());
        }
        else if(isIntConst(cur)) {
            add_analyse(new Number());
        }
    }
}
