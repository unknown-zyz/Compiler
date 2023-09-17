package Lexical;

public class Word {
    private String token;
    private TokenType type;
    private int line;

    public Word(String token, TokenType type, int line) {
        this.token = token;
        this.type = type;
        this.line = line;
    }

    public String getToken() {
        return token;
    }

    public TokenType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

}
