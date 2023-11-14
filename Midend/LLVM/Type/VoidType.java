package Midend.LLVM.Type;

public class VoidType extends Type{

    public static VoidType voidType = new VoidType();

    @Override
    public String toString(){
        return "void";
    }
}
