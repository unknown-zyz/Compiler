package Midend.LLVM.Type;

public class IntType extends Type{

    private final int bit;
    private IntType(int bit){
        this.bit = bit;
    }

    public static IntType I32 = new IntType(32);
    public static IntType I8 = new IntType(8);
    public static IntType I1 = new IntType(1);

    @Override
    public String toString(){
        return "i" + bit;
    }

    @Override
    public boolean isIntType(){
        return true;
    }
}
