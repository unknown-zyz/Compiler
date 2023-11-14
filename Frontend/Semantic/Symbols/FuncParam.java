package Frontend.Semantic.Symbols;

public class FuncParam {
    private String name;
    private int dimension;

    public FuncParam(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }
}
