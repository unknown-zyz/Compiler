package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import Error.error;

public class DeclNode extends non_Terminal {
    public void analyse() {
        if(cur.getToken().equals("int")) {
            Reserved bType = new Reserved(cur);
            child.add(bType);
        }
        else {
            new error();
        }
    }
}
