package LLVM.Value.Instruction;

import LLVM.Value.Value;
import LLVM.type.VoidType;

public class StoreInst extends Instruction{

    public StoreInst(Value value, Value pointer) {
        super("", VoidType.voidType, Operator.Store);
        this.addOperand(value);
        this.addOperand(pointer);
    }

    public Value getValue(){
        return getOperand(0);
    }

    public Value getPointer(){
        return getOperand(1);
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public String toString() {
        return "store " + getValue().getType() + " " + getValue().getName() + ", " + getPointer().getType() + " " + getPointer().getName();
    }
}
