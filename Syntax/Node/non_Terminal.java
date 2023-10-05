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
        String str = word.getToken();
        int len = str.length();
        if(str.charAt(0)!='\"'||str.charAt(len-1)!='\"')    return false;
        for (int i = 1; i < len-1; i++)
        {
            char ch = str.charAt(i);
            if(ch=='%')
            {
                if(str.charAt(i+1)!='d')
                    return false;
                i++;continue;
            }
            if(ch=='\\')
            {
                if(str.charAt(i+1)!='n')
                    return false;
                i++;continue;
            }
            if ((int)ch != 32 && (int)ch != 33 && ((int)ch < 40 || (int)ch > 126)) {
                return false;
            }
        }
        return true;
    }
    //后序遍历
    public void print() {
        for(ASTNode node: getChild()) {
            node.print();
        }
        System.out.println("<"+this.getClass().getSimpleName()+">");
    }

    public void printChild(int step) {
        for(int i=0;i<step;i++)
            System.out.print("\t");
        System.out.println("<"+this.getClass().getSimpleName()+">");
        for(ASTNode node: getChild())
            node.printChild(step+1);

    }
}