package Midend.LLVM.Value;

import Midend.LLVM.Value.Instruction.Instruction;
import Midend.LLVM.Type.VoidType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final Function parentFunc;
    private final ArrayList<Instruction> insts;
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
