package Midend.LLVM.Value.Instruction;

import Midend.LLVM.Value.Value;
import Midend.LLVM.Type.PointerType;
import Midend.LLVM.Type.Type;

public class AllocInst extends Instruction{

    public AllocInst(Type type) {
        super("%" + (++Value.valCnt), type, Operator.Alloca);
    }

    public Type getAllocType() {
        return ((PointerType)getType()).getpType();
    }

    @Override
    public String toString() {
        return getName() + " = " + "alloca " + ((PointerType)getType()).getpType();
    }
}
