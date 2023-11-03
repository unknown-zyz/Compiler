package LLVM.Value;

import LLVM.type.Type;

public class GlobalVar extends Value {
    private final Value value;
    private final boolean isConst;

    public GlobalVar(String name, Type type, Value value, boolean isConst) {
        super(name, type);
        this.value = value;
        this.isConst = isConst;
    }

    public Value getValue() {
        return value;
    }

    public boolean isConst() {
        return isConst;
    }
}
