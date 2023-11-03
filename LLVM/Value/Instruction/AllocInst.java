package LLVM.Value.Instruction;

import LLVM.Value.Value;
import LLVM.type.PointerType;
import LLVM.type.Type;

public class AllocInst extends Instruction{

    public AllocInst(Type type) {
        super("%" + (++Value.valCnt), type, Operator.Alloca);
    }

    @Override
    public String toString() {
        return getName() + " = " + "alloca " + ((PointerType)getType()).getpType();
    }
}
