package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class FuncType extends non_Terminal {
    @Override
    public void analyse() {
        addChild(new Reserved(cur));
        next();
    }
}
