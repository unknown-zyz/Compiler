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

    public Value getOperand(int pos){
        return operandList.get(pos);
    }
}
