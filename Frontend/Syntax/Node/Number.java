package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class Number extends non_Terminal {
    @Override
    public void analyse() {
        addChild(new IntConst(cur));
        next();
    }
}
