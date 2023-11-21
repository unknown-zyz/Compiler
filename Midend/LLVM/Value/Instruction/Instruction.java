package Midend.LLVM.Value.Instruction;


import Midend.LLVM.Value.BasicBlock;
import Midend.LLVM.Value.User;
import Midend.LLVM.Type.Type;

public class Instruction extends User {
    private Operator op;
    private BasicBlock parentBB;
    private boolean isDeleted;
    public Instruction(String name, Type type, Operator op) {
        super(name, type);
        this.op = op;
        this.isDeleted = false;
    }

    public Operator getOp() {
        return op;
    }

    public BasicBlock getParentBB() {
        return parentBB;
    }

    public void setParentBB(BasicBlock parentBB) {
        this.parentBB = parentBB;
    }

    public boolean hasValue() {
        return true;
    }

    public void delete() {
        removeUse();
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
