package LLVM.Value;

import LLVM.type.IntType;
import LLVM.type.Type;

public class ConstInteger extends Value{
    private int value;
    public ConstInteger(String value, Type type) {
        super(value, type);
        this.value = Integer.parseInt(value);
    }

    public int getValue() {
        return value;
    }
}
