package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class MainFuncDef extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("int"))
        {
            addChild(new Reserved(cur));
            next();
            if(cur_equal("main"))
            {
                addChild(new Reserved(cur));
                next();
                if(cur_equal("("))
                {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal(")"))
                    {
                        addChild(new Symbol(cur));
                        next();
                        add_analyse(new Block());
                    }
                    else {}
                }
                else {}
            }
            else {}
        }
        else {}
    }
}
