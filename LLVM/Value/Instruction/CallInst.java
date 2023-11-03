package LLVM.Value.Instruction;

import LLVM.Value.Function;
import LLVM.Value.Value;
import LLVM.type.VoidType;

import java.util.ArrayList;

public class CallInst extends Instruction{
    private Function function;
    private final boolean hasValue;

    public CallInst(Function function, ArrayList<Value> values) {
        super("%" + (++Value.valCnt), function.getType(), Operator.Call);
        this.function = function;
        this.hasValue = (function.getType() != VoidType.voidType);
        for (Value value : values) {
            addOperand(value);
        }
    }


    @Override
    public boolean hasValue() {
        return this.hasValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(hasValue){
            sb.append(getName()).append(" = ");
        }
        sb.append("call ").append(function.getType()).append(" @").append(function.getName()).append("(");
        ArrayList<Value> operands = getOperands();
        for(int i = 0; i < operands.size(); i++) {
            Value value = operands.get(i);
            sb.append(value.getType()).append(" ").append(value.getName());
            if(i != operands.size() - 1){
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }


}
