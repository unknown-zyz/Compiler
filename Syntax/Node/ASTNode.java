package Syntax.Node;

public abstract class ASTNode {
    public abstract void print();

    public abstract void printChild(int step);

    public boolean cur_equal(String s) {
        return Syntax.SyntaxMain.cur.getToken().equals(s);
    }


}
