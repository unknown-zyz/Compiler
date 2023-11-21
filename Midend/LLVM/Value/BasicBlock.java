package Midend.LLVM.Value;

import Midend.LLVM.Value.Instruction.Instruction;
import Midend.LLVM.Type.VoidType;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends Value {
    private final Function parentFunc;
    private final ArrayList<Instruction> insts;
    public static int blockCnt = 0;
    private ArrayList<BasicBlock> preBlocks;
    private ArrayList<BasicBlock> nxtBlocks;
    private HashSet<BasicBlock> strictDomors;
    private BasicBlock iDominator;
    private HashSet<BasicBlock> df;

    public BasicBlock(Function function) {
        super("l"+blockCnt++, VoidType.voidType);
        parentFunc = function;
        insts = new ArrayList<>();
    }

    public void addInst(Instruction inst) {
        insts.add(inst);
    }

    public void addInstToHead(Instruction inst) {
        insts.add(0, inst);
    }

    public ArrayList<Instruction> getInsts() {
        return insts;
    }

    public Instruction getLastInst() {return insts.get(insts.size()-1);}

    public void setPreBlocks(ArrayList<BasicBlock> preBlocks) {
        this.preBlocks = preBlocks;
    }

    public void setNxtBlocks(ArrayList<BasicBlock> nxtBlocks) {
        this.nxtBlocks = nxtBlocks;
    }

    public ArrayList<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    public ArrayList<BasicBlock> getNxtBlocks() {
        return nxtBlocks;
    }

    public HashSet<BasicBlock> getStrictDomors() {
        return strictDomors;
    }

    public void setStrictDomors(HashSet<BasicBlock> strictDomors) {
        this.strictDomors = strictDomors;
    }

    public BasicBlock getIDominator() {
        return iDominator;
    }

    public void setIDominator(BasicBlock iDominator) {
        this.iDominator = iDominator;
    }

    public HashSet<BasicBlock> getDf() {
        return df;
    }

    public void setDf(HashSet<BasicBlock> df) {
        this.df = df;
    }

    //待实现
    public void remove() {

    }
}
