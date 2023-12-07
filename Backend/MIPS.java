package Backend;

import Midend.LLVM.IRModule;
import Midend.LLVM.Value.BasicBlock;
import Midend.LLVM.Value.Function;
import Midend.LLVM.Value.GlobalVar;
import Midend.LLVM.Value.Instruction.*;
import Midend.LLVM.Value.Value;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static Config.Config.MipsPath;

public class MIPS {
    private BufferedWriter out;
    private final mMap symbolTable = new mMap();
    private final ArrayList<Reg> regA = new ArrayList<>();      // a0:调用GETINT，PUTINT会用到，目前认为不会冲突？
    private final ArrayList<Reg> regT = new ArrayList<>();
    private final ArrayList<Reg> regS = new ArrayList<>();
    private final HashMap<Reg, Reg> savedReg = new HashMap<>();
    private int sp = 0;
    private int ra = 0;
    private int varCnt = 0;

    public void printSymbolTable() {
        for (String str : symbolTable.keySet()) {
            if (symbolTable.get(str) instanceof Memory memory)
                System.out.println(str + " " + memory.getAddr() + memory.getOff());
            else if (symbolTable.get(str) instanceof Reg reg)
                System.out.println(str + " " + reg.getName());
        }
    }

    public void initReg() {
        for (int i = 0; i < 4; i++) {
            Reg reg = new Reg("$a" + i);
            regA.add(reg);
        }
        for (int i = 0; i < 10; i++) {
            Reg reg = new Reg("$t" + i);
            regT.add(reg);
        }
        for (int i = 0; i < 8; i++) {
            Reg reg = new Reg("$s" + i);
            regS.add(reg);
        }
    }

    public Reg getReg() {
        for (Reg reg : regT) {
            if (!reg.isUsed()) {
                reg.use();
                return reg;
            }
        }
        System.out.println("getReg error");
        return null;
    }

    public Reg getFuncReg() {
        for (Reg reg : regA) {
            if (!reg.isUsed()) {
                reg.use();
                return reg;
            }
        }
        return getReg();
    }

    public Reg getSavedReg() {
        for (Reg reg : regS) {
            if (!reg.isUsed()) {
                reg.use();
                return reg;
            }
        }
        System.out.println("getSavedReg error");
        return null;
    }

    //todo:保存的寄存器仍可能被调用           用s0-s7存可以解决，但要求不能超过8个需要保存的
    public void saveRegs() {
        ArrayList<Reg> usedRegs = new ArrayList<>();
        for (Reg reg : regT) {
            if (reg.isUsed()) {
                usedRegs.add(reg);
            }
        }
        for (Reg reg : usedRegs) {
            Reg reg1 = getSavedReg();
            savedReg.put(reg, reg1);
            write("\tmove " + reg1.getName() + ", " + reg.getName() + "\n");
        }
    }

    public void restoreRegs() {
        for (Reg reg : savedReg.keySet()) {
            Reg reg1 = savedReg.get(reg);
            write("\tmove " + reg.getName() + ", " + reg1.getName() + "\n");
            reg1.clear();
        }
        savedReg.clear();
    }

    public Reg symbol2Reg(String sym) {         //sym: 1,2... or %1,%2..
        Reg reg;
        if (isNumber(sym)) {
            reg = getReg();
            load(reg.getName(), sym);
        } else {
            reg = (Reg) symbolTable.get(sym);
        }
        return reg;
    }

    public void run(IRModule module) throws IOException {
        out = new BufferedWriter(new FileWriter(MipsPath));
        write(".macro GETINT()\nli $v0, 5\nsyscall\n.end_macro\n");
        write("\n.macro PUTINT()\nli $v0, 1\nsyscall\n.end_macro\n");
        write("\n.macro PUTCH()\nli $v0, 11\nsyscall\n.end_macro\n");
        write("\n.macro PUTSTR()\nli $v0, 4\nsyscall\n.end_macro\n");
        write("\n.data\n");
        for (GlobalVar globalVar : module.getGlobalVars()) {
            write("\t" + globalVar.toMIPS() + "\n");
            getGp(globalVar.getName(), globalVar);
        }
        for (int i = 0; i < module.getStringList().size(); i++) {       //todo:没有真用到
            write("\tstr" + i + ": .asciiz \"" + module.getStringList().get(i) + "\"\n");
        }
        write("\n.text\n");
        write("\njal main\n");
        write("j return\n");
        initReg();
        for (Function function : module.getFunctions()) {
            varCnt = 0; sp = 0; ra = sp;
            write("\n" + function.getName() + ":\n");
            store("$ra", "$sp", ra);
            sp -= 4;
            int arg = function.getArgs().size();
            ArrayList<Reg> regs = new ArrayList<>();
            for (int i = 0; i < arg; i++) {
                Reg reg = getFuncReg();
                load(reg.getName(), "$sp", (arg - i) * 4);
                regs.add(reg);
                symbolTable.put(function.getArgs().get(i).getName(), reg);
            }
            for (Reg reg : regs) {
                reg.clear();
            }
            for (BasicBlock bb : function.getBasicBlocks()) {
                write(bb.getName()+":\n");
                boolean flag = false;
                for (Instruction inst : bb.getInsts()) {
//                    if (!(inst instanceof AllocInst)) {
//                        getSp(inst.getName(), inst);
//                    }
                    if(flag)    break;
                    if (inst instanceof RetInst || inst instanceof BrInst) {
                        flag = true;
                    }
                    translate(inst);
                }
            }
//            System.out.println(function.getName()+":");
//            printSymbolTable();
            symbolTable.clear();
        }
        write("\nreturn:\n");
        out.close();
    }

    private void translate(Instruction inst) {
        //reg在使用后就释放!!!!!!
        if (inst instanceof AllocInst) visitAllocInst();
        else if (inst instanceof BinaryInst) visitBinaryInst((BinaryInst) inst);
        else if (inst instanceof BrInst) visitBrInst((BrInst) inst);
        else if (inst instanceof CallInst) visitCallInst((CallInst) inst);
        else if (inst instanceof LoadInst) visitLoadInst((LoadInst) inst);
        else if (inst instanceof RetInst) visitRetInst((RetInst) inst);
        else if (inst instanceof StoreInst) visitStoreInst((StoreInst) inst);
    }

    public void load(String reg, String addr, int offset) {
        write("\tlw " + reg + ", " + offset + "(" + addr + ")\n");
    }

    public void store(String reg, String addr, int offset) {
        write("\tsw " + reg + ", " + offset + "(" + addr + ")\n");
    }

    public void load(String reg, String name) {
        if (isNumber(name)) {
            write("\tli " + reg + ", " + name + "\n");
        } else if (symbolTable.get(name) instanceof Memory memory) {
            if (memory.getValue() instanceof GlobalVar globalVar) {
                write("\tla " + reg + ", " + name.substring(1) + "\n");
                if (!globalVar.isArray()) {
                    write("\tlw " + reg + ", 0(" + reg + ")\n");
                }
            } else {
                write("\tlw " + reg + ", " + memory.getOff() + "(" + memory.getAddr() + ")\n");
            }
        } else if (symbolTable.get(name) instanceof Reg reg0) {
            write("\tmove " + reg + ", " + reg0.getName() + "\n");
            //!!!!待确定
            reg0.clear();
        }
    }

    public void store(String reg, String name) {
        Memory memory = (Memory) symbolTable.get(name);
        if (memory.getValue() instanceof GlobalVar globalVar) {
            Reg reg0 = getReg();
            write("\tla " + reg0.getName() + ", " + name.substring(1) + "\n");
            if (!globalVar.isArray()) {
                write("\tsw " + reg + ", 0(" + reg0.getName() + ")\n");
            }
            reg0.clear();
        } else {
            write("\tsw " + reg + ", " + memory.getOff() + "(" + memory.getAddr() + ")\n");
        }
    }

    //todo:rename
    public void getGp(String name, Value value) {
        if (symbolTable.containsKey(name)) {
            return;
        }
        symbolTable.put(name, new Memory("$gp", 0, value));
    }

    public void getSp(String name, Value value) {
        if (symbolTable.containsKey(name)) {
            return;
        }
        symbolTable.put(name, new Memory("$sp", sp, value));
        sp -= 4;
    }

    //用t0=t0+t1替代
    public void calc(Reg reg0, Reg reg1, Reg reg2, String op) {
        write("\t" + op + " " + reg0.getName() + ", " + reg1.getName() + ", " + reg2.getName() + "\n");
    }

    public void visitAllocInst() {
        varCnt++;
    }

    public void visitBinaryInst(BinaryInst inst) {
        Reg reg0 = getReg();
        Reg reg1 = symbol2Reg(inst.getOperand(0).getName());
        Reg reg2 = symbol2Reg(inst.getOperand(1).getName());

        if (inst.getOp() == Operator.Add) {
            calc(reg0, reg1, reg2, "addu");
        } else if (inst.getOp() == Operator.Sub) {
            calc(reg0, reg1, reg2, "subu");
        } else if (inst.getOp() == Operator.Mul) {
            calc(reg0, reg1, reg2, "mul");
        } else if (inst.getOp() == Operator.Div) {              //???  div $t1, $t2    Hi LO
            calc(reg0, reg1, reg2, "div");
        } else if (inst.getOp() == Operator.Mod) {
            calc(reg0, reg1, reg2, "rem");
        } else if (inst.getOp() == Operator.Eq) {
            calc(reg0, reg1, reg2, "seq");
        } else if (inst.getOp() == Operator.Ne) {
            calc(reg0, reg1, reg2, "sne");
        } else if (inst.getOp() == Operator.Lt) {
            calc(reg0, reg1, reg2, "slt");
        } else if (inst.getOp() == Operator.Le) {
            calc(reg0, reg1, reg2, "sle");
        } else if (inst.getOp() == Operator.Gt) {
            calc(reg0, reg1, reg2, "sgt");
        } else if (inst.getOp() == Operator.Ge) {
            calc(reg0, reg1, reg2, "sge");
        }
//        else if (inst.getOp() == Operator.Zext) {
//
//        }
        reg1.clear();
        reg2.clear();
        symbolTable.put(inst.getName(), reg0);
    }

    public void visitBrInst(BrInst inst) {
        if(inst.isJump()) {
            write("\tj "+inst.getJumpBlock().getName()+"\n");
        }
        else {
            Reg reg = (Reg) symbolTable.get(inst.getBooleanVal().getName());
            write("\tbeq "+reg.getName()+", $0, "+inst.getFalseBlock().getName()+"\n");
            write("\tj "+inst.getTrueBlock().getName()+"\n");
            reg.clear();
        }
    }

    public void visitCallInst(CallInst inst) {
        Function function = inst.getFunction();
        int size = function.getArgs().size();
        switch (function.getName()) {
            case "getint" -> {
                write("\tGETINT()\n");
                Reg reg = getReg();
                symbolTable.put(inst.getName(), reg);
                write("\tmove " + reg.getName() + ", $v0\n");
            }
            case "putint" -> {
                load("$a0", inst.getOperand(0).getName());
                write("\tPUTINT()\n");
            }
            case "putch" -> {
                load("$a0", inst.getOperand(0).getName());
                write("\tPUTCH()\n");
            }
            case "putstr" ->
                //todo
                    write("\tPUTSTR()\n");
            default -> {
                write("\tsubu $sp, $sp, " + (4 * (size + 1 + varCnt)) + "\n");
                for (int i = 0; i < size; i++) {
                    Reg reg = symbol2Reg(inst.getOperands().get(i).getName());
                    store(reg.getName(), "$sp", (size - i) * 4);
                    reg.clear();
                }
                saveRegs();
                write("\tjal " + function.getName() + "\n");
                restoreRegs();
                write("\taddu $sp, $sp, " + (4 * (size + 1 + varCnt)) + "\n");
                if (inst.hasValue()) {
                    Reg reg = getReg();
                    symbolTable.put(inst.getName(), reg);
                    write("\tmove " + reg.getName() + ", $v0\n");
                }
            }
        }
    }

    public void visitLoadInst(LoadInst inst) {
        Reg reg = getReg();
        symbolTable.put(inst.getName(), reg);
        load(reg.getName(), inst.getPointer().getName());
    }

    public void visitRetInst(RetInst inst) {
        load("$ra", "$sp", ra);
        if (!inst.getOperand(0).getType().isVoidType()) {
            load("$v0", inst.getOperand(0).getName());
        }
        write("\tjr $ra\n");
    }

    public void visitStoreInst(StoreInst inst) {
        Reg reg;
        if (isNumber(inst.getValue().getName())) {
            reg = getReg();
            load(reg.getName(), inst.getValue().getName());
        } else {
            reg = (Reg) symbolTable.get(inst.getValue().getName());
        }
        getSp(inst.getPointer().getName(), inst.getOperand(0));
        store(reg.getName(), inst.getPointer().getName());
        reg.clear();
    }

    public void write(String str) {
        try {
            out.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
