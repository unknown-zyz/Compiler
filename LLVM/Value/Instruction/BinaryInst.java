package LLVM.Value.Instruction;

import LLVM.Value.Value;
import LLVM.type.IntType;
import LLVM.type.Type;

public class BinaryInst extends Instruction{
    public BinaryInst(Operator op, Value left, Value right, Type type) {
        super("%" + (++Value.valCnt), type, op);
        this.addOperand(left);
        this.addOperand(right);
    }

    @Override
    public String toString() {
        Value left = getOperand(0);
        Value right = getOperand(1);
        Type type;
        if(left.getType() == IntType.I32 && right.getType() == IntType.I32)
            type = IntType.I32;
        else
            type = IntType.I1;
        return getName() + " = " + getOp() + " " + type + " " + left.getName() + ", " + right.getName();
    }
}
