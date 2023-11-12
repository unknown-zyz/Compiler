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
        assert left.getType() == right.getType();
        if(getOp() == Operator.Zext)
            return getName() + " = " + getOp() + " " + left.getType() + " " + left.getName() + " to " + right.getType();
        return getName() + " = " + getOp() + " " + left.getType() + " " + left.getName() + ", " + right.getName();
    }
}
