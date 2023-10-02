package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class Number extends non_Terminal {
    @Override
    public void analyse() {
        addChild(new IntConst(cur));
        next();
    }
}
