package Frontend.Lexical;

public class Word {
    private final String token;
    private final TokenType type;
    private final int line;

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
