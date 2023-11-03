package LLVM.type;

public class VoidType extends Type{

    public static VoidType voidType = new VoidType();

    @Override
    public String toString(){
        return "void";
    }
}
