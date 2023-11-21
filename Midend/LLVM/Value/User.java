package Midend.LLVM.Value;

import Midend.LLVM.Use;
import Midend.LLVM.Type.Type;

import java.util.ArrayList;

public class User extends Value {
    private final ArrayList<Value> operandList;

    public User(String name, Type type){
        super(name, type);
        this.operandList = new ArrayList<>();
    }

    public void addOperand(Value operand){
        operandList.add(operand);
        if (operand != null) {
            operand.addUse(new Use(this, operand));
        }
    }

    public ArrayList<Value> getOperands() {
        return operandList;
    }

    public Value getOperand(int index){
        return operandList.get(index);
    }

    public void setOperand(int index, Value operand) {
        if (index >= operandList.size()) {
            return;
        }
        operandList.set(index, operand);
        if (operand != null) {
            operand.addUse(new Use(this, operand));
        }
    }

    public void replaceOperand(int index, Value value) {
        setOperand(index, value);
        Value operand = operandList.get(index);
        if (operand != null) {
            operand.removeOneUse(this);
        }
    }

    public void removeUse() {
        for(Value operand: operandList) {
            if(operand != null)
                operand.removeOneUse(this);
        }
    }
}
