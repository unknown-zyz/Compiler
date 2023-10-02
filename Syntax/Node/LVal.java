package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

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
            else {}
        }
    }
}
