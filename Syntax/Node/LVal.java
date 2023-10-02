package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class LVal extends non_Terminal {
    @Override
    public void analyse() {
        addChild(new Ident(cur));
        next();
        while (cur_equal("[")) {
            addChild(new Symbol(cur));
            next();
            add_analyse(new Exp());
            if (cur_equal("]"))
            {
                addChild(new Symbol(cur));
                next();
            }
//            else
//                System.out.println("error k"+getBefore().getLine());
        }
    }
}
