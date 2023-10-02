package Syntax.Node;

public abstract class ASTNode {
    public abstract void print();

    public boolean cur_equal(String s) {
        return Syntax.SyntaxMain.cur.getToken().equals(s);
    }

    //get_next,get_next_next
}
