package Midend.LLVM.Value.Instruction;

import Midend.LLVM.Value.Value;
import Midend.LLVM.Type.VoidType;


public class RetInst extends Instruction {

    public RetInst(Value value) {
        super("", VoidType.voidType, Operator.Ret);
        this.addOperand(value);
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public String toString() {
        Value value = getOperand(0);
        return "ret " + value.getType() + " " + value.getName();
    }
}
