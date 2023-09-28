package Syntax.Node;

import Lexical.Word;


//非终结符结点  or  Btype，Ident， ....
public class Reserved extends Terminal {
//    private Word word;
//
    public Reserved(Word word) {
        this.word = word;
    }
}
