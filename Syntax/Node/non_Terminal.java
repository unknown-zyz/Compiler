package Syntax.Node;

import java.util.ArrayList;

public abstract class non_Terminal extends ASTNode {
    ArrayList<ASTNode> child = new ArrayList<>();
    public ArrayList<ASTNode> getChild() {
        return child;
    }

    //后序遍历
    public void print() {
        for(ASTNode node: getChild()) {
            node.print();
        }
        System.out.println(this.getClass().getSimpleName());
    }

}
