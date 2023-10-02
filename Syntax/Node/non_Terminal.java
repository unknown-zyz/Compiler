package Syntax.Node;

import Lexical.Word;

import java.util.ArrayList;

import static Lexical.Lexer.reserved;

public abstract class non_Terminal extends ASTNode {
    public abstract void analyse();

    ArrayList<ASTNode> child = new ArrayList<>();
    public ArrayList<ASTNode> getChild() {
        return child;
    }

    public void addChild(ASTNode node) {
        child.add(node);
    }

    public ASTNode removeChild() {
        return child.remove(child.size() - 1);
    }
    public void add_analyse(non_Terminal node) {
        child.add(node);
        node.analyse();
    }

    public static boolean isIdent(Word word) {
        return word.getToken().matches("^[a-zA-Z_][a-zA-Z0-9_]*$") && !reserved.containsKey(word.getToken());
    }

    public static boolean isIntConst(Word word) {
        return word.getToken().matches("^[0-9]+$");
    }

    public static boolean isFormatString(Word word) {
//        return word.getToken().matches("\"%([0-9]+\\$)?([-#+ 0,(]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])\"");
        return true;
    }

    //后序遍历
    public void print() {
        for(ASTNode node: getChild()) {
            node.print();
        }
        System.out.println("<"+this.getClass().getSimpleName()+">");
    }

    public void printChild() {
        for(ASTNode node: getChild())
        {
            if(node instanceof non_Terminal)
                ((non_Terminal)node).printChild();
        }
        System.out.println("<"+this.getClass().getSimpleName()+">"+getChild());
    }
}
