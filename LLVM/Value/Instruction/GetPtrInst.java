package LLVM.Value.Instruction;

import LLVM.Value.Value;
import LLVM.type.PointerType;
import LLVM.type.Type;

import java.util.ArrayList;

public class GetPtrInst extends Instruction {

    public GetPtrInst(Value value, ArrayList<Value> indexs, Type type) {
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
