package Midend.LLVM.Type;

public class PointerType extends Type {
    private final Type pType;

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
