package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class FuncDef extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("void")||cur_equal("int")) {
            add_analyse(new FuncType());
            if(isIdent(cur)) {
                addChild(new Ident(cur));
                next();
                if(cur_equal("(")) {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("int"))
                        add_analyse(new FuncFParams());
                    if(cur_equal(")")) {
                        addChild(new Symbol(cur));
                        next();
                        add_analyse(new Block());
                    }
//                    else
//                    add_analyse(new Block());     //????
//                        System.out.println("error j"+getBefore().getLine());
                }
            }
        }
    }
}
