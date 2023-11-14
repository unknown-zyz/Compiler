package Frontend.Semantic.Error;

public class Error {
    private final ErrorType errorType;
    private final int line;

    public Error(ErrorType errorType, int line) {
        this.errorType = errorType;
        this.line = line;
    }

    @Override
    public String toString() {
        return line + " " + errorType;
    }

}
