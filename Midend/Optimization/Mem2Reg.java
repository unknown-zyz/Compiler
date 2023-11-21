package Midend.Optimization;

import Midend.LLVM.IRFactory;
import Midend.LLVM.IRModule;
import Midend.LLVM.Value.BasicBlock;
import Midend.LLVM.Value.Function;
import Midend.LLVM.Value.Instruction.*;
import Midend.LLVM.Value.Value;

import java.util.*;

public class Mem2Reg {
    private final IRFactory f = IRFactory.getInstance();
    private final ArrayList<AllocInst> defs = new ArrayList<>();
    private HashMap<AllocInst, ArrayList<BasicBlock>> defMap = new HashMap<>();
    private final HashMap<PhiInst, AllocInst> PhiAllocMap = new HashMap<>();
    private final HashMap<BasicBlock, Boolean> visited = new HashMap<>();
    private HashMap<BasicBlock, ArrayList<BasicBlock>> idoms;
    private final HashSet<Instruction> deletedInst = new HashSet<>();

    public void run(IRModule module) {
        for (Function function : module.getFunctions()) {
            calculate(function);
            idoms = function.getIdoms();
            ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
            for (BasicBlock bb : basicBlocks) {
                for (Instruction inst : bb.getInsts()) {
                    if (inst instanceof AllocInst allocInst) {
                        if (allocInst.getAllocType().isIntType()) {
                            defs.add(allocInst);
                            defMap.put(allocInst, new ArrayList<>());
                        }
                    } else if (inst instanceof StoreInst storeInst) {
                        Value pointer = storeInst.getPointer();
                        if (pointer instanceof AllocInst && defMap.containsKey(pointer)) {
                            defMap.get(pointer).add(bb);
                        }
                    }
                }
            }
            HashMap<AllocInst, ArrayList<BasicBlock>> tmpDefMap = new HashMap<>(defMap);
            for (AllocInst allocInst : defMap.keySet()) {
                if (defMap.get(allocInst).size() == 0) {
                    defs.remove(allocInst);
                    tmpDefMap.remove(allocInst);
                }
            }
            defMap = tmpDefMap;
            for (AllocInst allocInst : defMap.keySet()) {
                HashSet<BasicBlock> F = new HashSet<>();
                Queue<BasicBlock> W = new LinkedList<>(defMap.get(allocInst));
                while (!W.isEmpty()) {
                    BasicBlock X = W.remove();
                    HashSet<BasicBlock> DF_X = X.getDf();
                    for (BasicBlock Y : DF_X) {
                        if (!F.contains(Y)) {
                            ArrayList<Value> tmpValues = new ArrayList<>(Collections.nCopies(Y.getPreBlocks().size(), f.buildNumber("0")));
                            PhiInst phiInst = f.buildPhiInst(Y, allocInst.getAllocType(), tmpValues);
                            PhiAllocMap.put(phiInst, allocInst);
                            F.add(Y);
                            if (!defMap.get(allocInst).contains(Y)) {
                                W.add(Y);
                            }
                        }
                    }
                }
            }
            ArrayList<Value> values = new ArrayList<>();
            for (int i = 0; i < defMap.size(); i++) {
                values.add(f.buildNumber("0"));
            }
            for (BasicBlock bb : function.getBasicBlocks()) {
                visited.put(bb, false);
            }
            BasicBlock bbEntry = function.getEntryBlock();
            dfs(bbEntry, values);
        }
    }

    public void calculate(Function function) {
        buildCFG(function);
        computeStrictDom(function);
        removeUselessBLock(function);
        computeIDom(function);
        computeDF(function);
    }

    public void buildCFG(Function function) {
        HashMap<BasicBlock, ArrayList<BasicBlock>> prev = new HashMap<>();
        HashMap<BasicBlock, ArrayList<BasicBlock>> next = new HashMap<>();
        for (BasicBlock bb : function.getBasicBlocks()) {
            prev.put(bb, new ArrayList<>());
            next.put(bb, new ArrayList<>());
        }
        for (BasicBlock bb : function.getBasicBlocks()) {
            if (bb.getLastInst() instanceof BrInst brInst) {
                if (brInst.isJump()) {
                    BasicBlock jumpBlock = brInst.getJumpBlock();
                    next.get(bb).add(jumpBlock);
                    prev.get(jumpBlock).add(bb);
                } else {
                    BasicBlock trueBlock = brInst.getTrueBlock();
                    BasicBlock falseBlock = brInst.getFalseBlock();
                    next.get(bb).add(trueBlock);
                    next.get(bb).add(falseBlock);
                    prev.get(trueBlock).add(bb);
                    prev.get(falseBlock).add(bb);
                }
            }
        }
        for (BasicBlock bb : function.getBasicBlocks()) {
            bb.setPreBlocks(prev.get(bb));
            bb.setNxtBlocks(next.get(bb));
        }
        function.setCfg(next);
    }

    public void computeStrictDom(Function function) {
        HashMap<BasicBlock, HashSet<BasicBlock>> domMap = new HashMap<>();
        for (BasicBlock bb : function.getBasicBlocks()) {
            domMap.put(bb, new HashSet<>(function.getBasicBlocks()));
        }
        domMap.put(function.getEntryBlock(), new HashSet<>() {{
            add(function.getEntryBlock());
        }});
        boolean changed;
        do {
            changed = false;
            for (BasicBlock bb : function.getBasicBlocks()) {
                if (bb == function.getEntryBlock()) continue;
                HashSet<BasicBlock> tmp = new HashSet<>(function.getBasicBlocks());
                for (BasicBlock pre : bb.getPreBlocks()) {
                    tmp.retainAll(domMap.get(pre));
                }
                tmp.add(bb);
                if (!tmp.equals(domMap.get(bb))) {
                    domMap.put(bb, tmp);
                    changed = true;
                }
            }
        } while (changed);
        //strict dominate need to remove itself
        for (BasicBlock bb : function.getBasicBlocks()) {
            domMap.get(bb).remove(bb);
        }
        function.setStrictDoms(domMap);
//        for(BasicBlock bb: function.getBasicBlocks()) {
//            System.out.print(bb.getName()+":");
//            for(BasicBlock domBB:domMap.get(bb))
//                System.out.print(domBB.getName() + " ");
//            System.out.println();
//        }
    }

    public void removeUselessBLock(Function function) {
        Queue<BasicBlock> q = new LinkedList<>();
        ArrayList<BasicBlock> allBlocks = new ArrayList<>();
        HashSet<BasicBlock> allBlocksSet = new HashSet<>();
        q.add(function.getEntryBlock());
        allBlocks.add(function.getEntryBlock());
        allBlocksSet.add(function.getEntryBlock());
        while (!q.isEmpty()){
            BasicBlock nowBb = q.poll();
            for(BasicBlock nxtBb : nowBb.getNxtBlocks()){
                if(!allBlocksSet.contains(nxtBb)){
                    q.add(nxtBb);
                    allBlocks.add(nxtBb);
                    allBlocksSet.add(nxtBb);
                }
            }
        }
        ArrayList<BasicBlock> deletedBlock = new ArrayList<>();
        for(BasicBlock bb:function.getBasicBlocks()) {
            if(!allBlocksSet.contains(bb)){
                deletedBlock.add(bb);
            }
        }
        //待实现
        for(BasicBlock bb : deletedBlock){
            bb.remove();
        }
    }
    public void computeIDom(Function function) {
        boolean flag;
        HashMap<BasicBlock, HashSet<BasicBlock>> domMap = function.getStrictDoms();
        HashMap<BasicBlock, ArrayList<BasicBlock>> idomMap = new HashMap<>();
        for (BasicBlock bb : domMap.keySet()) {
            idomMap.put(bb, new ArrayList<>());
        }
        for (BasicBlock bb : domMap.keySet()) {
            HashSet<BasicBlock> strictDom = domMap.get(bb);
            for (BasicBlock bb1 : strictDom) {
                flag = true;
                for (BasicBlock bb2 : strictDom) {
                    if (bb2 == bb1) continue;
                    if (domMap.get(bb2).contains(bb1)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) idomMap.get(bb1).add(bb);
            }
        }
        function.setIdoms(idomMap);
        for(BasicBlock bb: idomMap.keySet()) {
            System.out.print(bb.getName()+":");
            for(BasicBlock son: idomMap.get(bb))
                System.out.print(son.getName() + " ");
            System.out.println();
        }
    }

    public void computeDF(Function function) {
        HashMap<BasicBlock, HashSet<BasicBlock>> df = new HashMap<>();
        for (BasicBlock bb : function.getBasicBlocks()) {
            df.put(bb, new HashSet<>());
        }
        HashMap<BasicBlock, ArrayList<BasicBlock>> cfg = function.getCfg();
        for (BasicBlock a : cfg.keySet()) {
            for (BasicBlock b : cfg.get(a)) {
                BasicBlock x = a;
                while (!b.getStrictDomors().contains(x)) {
                    df.get(x).add(b);
                    x = x.getIDominator();
                }
            }
        }
        function.setDfs(df);
//        for(BasicBlock bb: df.keySet())
//        {
//            System.out.print(bb.getName()+":");
//            for(BasicBlock dfBB:df.get(bb))
//                System.out.print(dfBB.getName() + " ");
//            System.out.println();
//        }
    }

    public void dfs(BasicBlock curBB, ArrayList<Value> values) {
        ArrayList<Value> tmpValues = new ArrayList<>(values);
//        System.out.println(curBB.getName());
        visited.put(curBB, true);
        deletedInst.clear();
        for (Instruction inst : curBB.getInsts()) {
            if (inst instanceof AllocInst allocInst) {
                if (defMap.containsKey(allocInst))
                    deletedInst.add(inst);
            } else if (inst instanceof LoadInst loadInst) {
                if (!(loadInst.getPointer() instanceof AllocInst allocInst && allocInst.getAllocType().isIntType())) {
                    continue;
                }
                Value value = tmpValues.get(defs.indexOf(allocInst));
//                System.out.println(allocInst);
//                System.out.println(value);
                loadInst.replace(value);
                deletedInst.add(inst);
            } else if (inst instanceof StoreInst storeInst) {
                if (!(storeInst.getPointer() instanceof AllocInst allocInst && allocInst.getAllocType().isIntType()))
                    continue;
                int index = defs.indexOf(allocInst);
                tmpValues.set(index, storeInst.getValue());
//                System.out.println(allocInst +","+ storeInst.getValue());
                deletedInst.add(storeInst);
            } else if (inst instanceof PhiInst phiInst) {
                AllocInst allocInst = PhiAllocMap.get(phiInst);
                if (allocInst != null) {
                    int index = defs.indexOf(allocInst);
                    tmpValues.set(index, phiInst);
                }
            }
        }
        for (BasicBlock nxtBB : curBB.getNxtBlocks()) {
            for (Instruction inst : nxtBB.getInsts()) {
                if (inst instanceof PhiInst phiInst) {
                    AllocInst allocInst = PhiAllocMap.get(phiInst);
                    if (allocInst != null) {
                        int preIndex = nxtBB.getPreBlocks().indexOf(curBB);
                        Value value = tmpValues.get(defs.indexOf(allocInst));
                        phiInst.replaceOperand(preIndex, value);
                    }
                }
            }
        }
        for (Instruction inst : deletedInst) {
            inst.delete();
        }
        for (BasicBlock bb : idoms.get(curBB)) {
            if (!visited.get(bb)) {
                dfs(bb, tmpValues);
            }
        }
    }
}
