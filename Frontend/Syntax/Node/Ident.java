package Frontend.Syntax.Node;

import Frontend.Lexical.Word;

public class Ident extends Terminal {

    private int dim = 0;

    public Ident(Word word) {
        this.word = word;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }
}
