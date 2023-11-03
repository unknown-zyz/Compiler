package LLVM.Value;

import LLVM.type.Type;

import java.util.ArrayList;

public class Function extends Value {
    private BasicBlock basicBlock;
    private final ArrayList<Argument> args;
    public Function(String name, Type type) {
        super(name, type);
        this.args = new ArrayList<>();
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public ArrayList<Argument> getArgs() {
        return args;
    }

    public void addArg(Argument arg) {
        args.add(arg);
    }
}
