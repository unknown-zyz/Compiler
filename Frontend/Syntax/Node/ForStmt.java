package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class ForStmt extends non_Terminal {
    @Override
    public void analyse() {
        if(isIdent(cur))
        {
            add_analyse(new LVal());
            if(cur_equal("="))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new Exp());
            }
        }

    }
}
