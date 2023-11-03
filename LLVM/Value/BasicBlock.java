package LLVM.Value;

import LLVM.Value.Instruction.Instruction;
import LLVM.type.VoidType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private Function parentFunc;
    private ArrayList<Instruction> insts;
    public static int blockCnt = 0;
    public BasicBlock(Function function) {
        super("block"+blockCnt++, VoidType.voidType);
        parentFunc = function;
        insts = new ArrayList<>();
    }

    public void addInst(Instruction inst) {
        insts.add(inst);
    }

    public ArrayList<Instruction> getInsts() {
        return insts;
    }
}
