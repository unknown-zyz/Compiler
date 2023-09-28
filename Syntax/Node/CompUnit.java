package Syntax.Node;

import Error.error;
import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;


public class CompUnit extends non_Terminal {
    public void analyse() {
        if(cur.getToken().equals("int")) {
            DeclNode declNode = new DeclNode();
            child.add(declNode);
            declNode.analyse();
        }
        else {
            new error();
        }
        next();
        Ident ident = new Ident(cur);
        child.add(ident);
        next();
        Symbol symbol = new Symbol(cur);
        child.add(symbol);
    }
}
