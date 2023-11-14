package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class FuncRParams extends non_Terminal {
    @Override
    public void analyse() {
        add_analyse(new Exp());
        while (cur_equal(",")) {
            addChild(new Symbol(cur));
            next();
            add_analyse(new Exp());
        }
    }
}
