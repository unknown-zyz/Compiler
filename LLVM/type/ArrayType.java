package LLVM.type;

public class ArrayType extends Type{
    int size;
    Type elementType;   //I32, [I32]

    public ArrayType(int size, Type elementType){
        this.size = size;
        this.elementType = elementType;
    }

    public int getSize() {
        return size;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean isArrayType(){
        return true;
    }

    @Override
    public String toString(){
        return "[" + size + " x " + elementType + "]";
    }
}
