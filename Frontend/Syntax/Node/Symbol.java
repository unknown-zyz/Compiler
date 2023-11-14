package Frontend.Syntax.Node;

import Frontend.Lexical.Word;

public class Symbol extends Terminal{
    public Symbol(Word word) {
        this.word = word;
    }
}
