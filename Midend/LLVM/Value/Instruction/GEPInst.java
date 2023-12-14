package Midend.LLVM.Value.Instruction;

import Midend.LLVM.Value.Value;
import Midend.LLVM.Type.PointerType;
import Midend.LLVM.Type.Type;

import java.util.ArrayList;

public class GEPInst extends Instruction {

    public GEPInst(Value value, ArrayList<Value> indexs, Type type) {
        super("%" + (++Value.valCnt), type, Operator.GEP);
        this.addOperand(value);
        for (Value index : indexs) {
            this.addOperand(index);
        }
    }

    public Value getValue() {
        return getOperand(0);
    }

    public ArrayList<Value> getIndexs() {
        ArrayList<Value> indexs = new ArrayList<>();
        for (int i = 1; i < getOperands().size(); i++) {
            indexs.add(getOperand(i));
        }
        return indexs;
    }

    public Value getLastIndex() {
        return getOperand(getOperands().size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = getelementptr ");
        Value value = getValue();
        PointerType pointerType = (PointerType) value.getType();
        sb.append(pointerType.getpType()).append(", ");
        sb.append(pointerType).append(" ");
        sb.append(value.getName());
        ArrayList<Value> indexs = getIndexs();
        for (Value index : indexs) {
            sb.append(", i32 ").append(index.getName());
        }
        return sb.toString();
    }
}
