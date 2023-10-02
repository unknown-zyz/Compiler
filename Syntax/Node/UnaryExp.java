package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class UnaryExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("+") || cur_equal("-") || cur_equal("!")) {
            add_analyse(new UnaryOp());
            add_analyse(new UnaryExp());
        }
        else if(cur_equal("(") || isIntConst(cur)) {
            add_analyse(new PrimaryExp());
        }
        else if(isIdent(cur)) {
            if(getNext().getToken().equals("(")) {
                addChild(new Ident(cur));
                next();
                addChild(new Symbol(cur));
                next();
                if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
                    add_analyse(new FuncRParams());
                if(cur_equal(")")) {
                    addChild(new Symbol(cur));
                    next();
                }
                else {}
            }
            else {
                add_analyse(new PrimaryExp());
            }
        }
        else {}
    }
}
