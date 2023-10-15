package Error;

public class Error {
    private ErrorType errorType;
    private int line;

    public Error(ErrorType errorType, int line) {
        this.errorType = errorType;
        this.line = line;
    }

    @Override
    public String toString() {
        return line + " " + errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getLine() {
        return line;
    }
}
