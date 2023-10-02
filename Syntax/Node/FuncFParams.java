package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class FuncFParams extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("int")) {
            add_analyse(new FuncFParam());
            while (cur_equal(",")) {
                addChild(new Symbol(cur));
                next();
                add_analyse(new FuncFParam());
            }
        }
        else {}
    }
}
