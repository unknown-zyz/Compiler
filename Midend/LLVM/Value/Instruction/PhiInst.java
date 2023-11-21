package Midend.LLVM.Value.Instruction;

import Midend.LLVM.Type.Type;
import Midend.LLVM.Value.Value;

import java.util.ArrayList;

public class PhiInst extends Instruction {
    public PhiInst(Type type, ArrayList<Value> values) {
        super("%" + (++Value.valCnt), type, Operator.Phi);
        for(Value value : values){
            addOperand(value);
        }
    }

    @Override
    public String toString() {
        ArrayList<Value> values = getOperands();
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = phi ").append(getType()).append(" ");
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            if (i != 0) {
                sb.append(",");
            }
            sb.append("[ ").append(value.getName()).append(", ").append("%").append(getParentBB().getPreBlocks().get(i).getName()).append(" ]");
        }
        return sb.toString();
    }
}
