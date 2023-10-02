package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class ConstDecl extends non_Terminal {
    public void analyse() {
        if(cur.getToken().equals("const")) {
            addChild(new Reserved(cur));
            next();
            if(cur.getToken().equals("int")) {
                addChild(new Reserved(cur));
                next();
                add_analyse(new ConstDef());
                while(cur.getToken().equals(",")) {
                    addChild(new Symbol(cur));
                    next();
                    add_analyse(new ConstDef());
                }
                if(cur.getToken().equals(";")) {
                    addChild(new Symbol(cur));
                    next();
                }
                else {}
            }
            else {}
        }
        else {}
    }
}
