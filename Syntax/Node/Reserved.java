package Syntax.Node;

import Lexical.Word;

public class Reserved extends Terminal {
    public Reserved(Word word) {
        this.word = word;
    }
}
