package Syntax.Node;

import Lexical.Word;

public abstract class Terminal extends ASTNode {
    Word word;
    public void print() {
        System.out.println(word.getType() + " " + word.getToken());
    }

}
