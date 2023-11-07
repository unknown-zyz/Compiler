package LLVM.Value.Instruction;


import LLVM.Value.User;
import LLVM.type.Type;

public class Instruction extends User {
    private Operator op;
    public Instruction(String name, Type type, Operator op) {
        super(name, type);
        this.op = op;
    }

    public Operator getOp() {
        return op;
    }

    public boolean hasValue() {
        return true;
    }
}