package Syntax.Node;

import static Syntax.SyntaxMain.*;

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
//                else
//                    System.out.println("error i"+getBefore().getLine());
            }
        }
    }
}
