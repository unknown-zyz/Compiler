package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class FuncFParams extends non_Terminal {
    @Override
    public void analyse()
    {
        if(cur_equal("int")) {
            add_analyse(new FuncFParam());
            while (cur_equal(","))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new FuncFParam());
            }
        }
    }
}
