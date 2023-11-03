package LLVM.type;

public class PointerType extends Type {
    private Type pType;

    public PointerType(Type pType) {
        this.pType = pType;
    }

    public Type getpType() {
        return pType;
    }

    @Override
    public String toString(){
        return pType.toString() + "*";
    }
}
