package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class ConstDef extends non_Terminal {
    public void analyse() {
        if(isIdent(cur))
        {
            addChild(new Ident(cur));
            next();
            while(cur_equal("["))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new ConstExp());
                if(cur_equal("]"))
                {
                    addChild(new Symbol(cur));
                    next();
                }
                else {}
            }
            if(cur_equal("="))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new ConstInitVal());
            }
            else {}
        }
        else {}
    }
}
