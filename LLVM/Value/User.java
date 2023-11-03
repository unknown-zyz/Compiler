package LLVM.Value;

import LLVM.Use;
import LLVM.Value.Value;
import LLVM.type.Type;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operandList;
    private int numOperands;

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
