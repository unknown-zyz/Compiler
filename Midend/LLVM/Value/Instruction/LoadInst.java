package Midend.LLVM.Value.Instruction;

import Midend.LLVM.Value.Value;
import Midend.LLVM.Type.Type;

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
