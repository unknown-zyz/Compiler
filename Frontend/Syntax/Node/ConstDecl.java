package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.*;
import Frontend.Semantic.Error.ErrorType;

public class ConstDecl extends non_Terminal {
    public void analyse() {
        if(cur_equal("const")) {
            addChild(new Reserved(cur));
            next();
            if(cur_equal("int")) {
                addChild(new Reserved(cur));
                next();
                add_analyse(new ConstDef());
                while(cur_equal(",")) {
                    addChild(new Symbol(cur));
                    next();
                    add_analyse(new ConstDef());
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
}
