package Syntax.Node;

import Lexical.Word;

public class Ident extends Terminal {
    public Ident(Word word) {
        this.word = word;
    }
}
