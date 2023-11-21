package Midend.LLVM.Value;

import Midend.LLVM.Type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends Value {
    private final ArrayList<BasicBlock> bbs;
    private final ArrayList<Argument> args;
    private HashMap<BasicBlock, HashSet<BasicBlock>> strictDoms;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> idoms;  //bb1支配bb2中的每一个bb
    private HashMap<BasicBlock, ArrayList<BasicBlock>> cfg;
    private HashMap<BasicBlock, HashSet<BasicBlock>> dfs;

    public Function(String name, Type type) {
        super(name, type);
        this.args = new ArrayList<>();
        this.bbs = new ArrayList<>();
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return bbs;
    }

    public BasicBlock getEntryBlock() {
        return bbs.get(0);
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

    public void setStrictDoms(HashMap<BasicBlock, HashSet<BasicBlock>> strictDoms) {
        this.strictDoms = strictDoms;
        for(BasicBlock bb : strictDoms.keySet()){
            bb.setStrictDomors(strictDoms.get(bb));
        }
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getStrictDoms() {
        return strictDoms;
    }

    public void setIdoms(HashMap<BasicBlock, ArrayList<BasicBlock>> idoms) {
        this.idoms = idoms;
        for(BasicBlock bb : idoms.keySet()) {
            for(BasicBlock son: idoms.get(bb))
                son.setIDominator(bb);
        }
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getIdoms() {
        return idoms;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getCfg() {
        return cfg;
    }

    public void setCfg(HashMap<BasicBlock, ArrayList<BasicBlock>> cfg) {
        this.cfg = cfg;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getDfs() {
        return dfs;
    }

    public void setDfs(HashMap<BasicBlock, HashSet<BasicBlock>> dfs) {
        this.dfs = dfs;
        for (BasicBlock bb : dfs.keySet()) {
            bb.setDf(dfs.get(bb));
        }
    }
}
