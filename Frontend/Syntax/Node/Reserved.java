package Frontend.Syntax.Node;

import Frontend.Lexical.Word;

public class Reserved extends Terminal {
    public Reserved(Word word) {
        this.word = word;
    }
}
