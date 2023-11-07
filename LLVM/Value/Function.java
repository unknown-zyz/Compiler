package LLVM.Value;

import LLVM.type.Type;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<BasicBlock> bbs;
    private final ArrayList<Argument> args;
    public Function(String name, Type type) {
        super(name, type);
        this.args = new ArrayList<>();
        this.bbs = new ArrayList<>();
    }

    public ArrayList<BasicBlock> getBasicBlock() {
        return bbs;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.bbs.add(basicBlock);
    }

    public ArrayList<Argument> getArgs() {
        return args;
    }

    public void addArg(Argument arg) {
        args.add(arg);
    }
}
