package Syntax.Node;

import Lexical.Word;

public abstract class Terminal extends ASTNode {
    Word word;
    public void print() {
        System.out.println(word.getType() + " " + word.getToken());
    }

    public void printChild(int step) {
        for(int i=0;i<step;i++)
            System.out.print("\t");
        System.out.println(word.getToken());
    }
}
