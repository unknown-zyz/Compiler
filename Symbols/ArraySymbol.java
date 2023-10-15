package Symbols;

public class ArraySymbol extends Symbol {
    private boolean isConst;
    private int dimension;

    public ArraySymbol(String name, boolean isConst, int dimension) {
        super(name);
        this.isConst = isConst;
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "ArraySymbol{" +
                "name=" + getName() +
                ", isConst=" + isConst +
                ", dimension=" + dimension +
                '}';
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }
}
