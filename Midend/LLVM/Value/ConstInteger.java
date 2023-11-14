package Midend.LLVM.Value;

import Midend.LLVM.Type.Type;

public class ConstInteger extends Value{
    private final int value;
    public ConstInteger(String value, Type type) {
        super(value, type);
        this.value = Integer.parseInt(value);
    }

    public int getValue() {
        return value;
    }
}
