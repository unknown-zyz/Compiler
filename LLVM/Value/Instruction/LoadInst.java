package LLVM.Value.Instruction;

import LLVM.Value.Value;
import LLVM.type.Type;

public class LoadInst extends Instruction {
    public LoadInst(Value pointer, Type type) {
        super("%" + (++Value.valCnt), type, Operator.Load);
        this.addOperand(pointer);
    }

    public Value getPointer() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        return getName() + " = load " + getType() + ", " + getPointer().getType() + " " + getPointer().getName();
    }
}
