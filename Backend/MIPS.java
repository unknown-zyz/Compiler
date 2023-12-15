package Backend;

import Midend.LLVM.IRModule;
import Midend.LLVM.Type.ArrayType;
import Midend.LLVM.Type.IntType;
import Midend.LLVM.Type.PointerType;
import Midend.LLVM.Type.Type;
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
    private final HashMap<Reg, Memory> savedReg = new HashMap<>();
    private int sp = 0;
    private int ra = 0;
    private int varCnt = 0;
    private StringBuilder sb = new StringBuilder();

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
        for (Reg reg : regS) {
            if (!reg.isUsed()) {
                reg.use();
                return reg;
            }
        }
        //todo:reg不够就放内存
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

//    public Reg getSavedReg() {
//        for (Reg reg : regS) {
//            if (!reg.isUsed()) {
//                reg.use();
//                return reg;
//            }
//        }
//        System.out.println("getSavedReg error");
//        return null;
//    }

    public int saveRegs() {
        ArrayList<Reg> usedRegs = new ArrayList<>();
        for (Reg reg : regT) {
            if (reg.isUsed()) {
                usedRegs.add(reg);
            }
        }
        for (Reg reg : regS) {
            if (reg.isUsed()) {
                usedRegs.add(reg);
            }
        }
        for (Reg reg : usedRegs) {
            Memory memory = new Memory("$sp", sp, new Value("", IntType.I32));
            savedReg.put(reg, memory);
            write("\tsw " + reg.getName() + ", " +sp+ "($sp)\n");
            sp-=4;
        }
        return usedRegs.size();
    }

    public void restoreRegs() {
        for (Reg reg : savedReg.keySet()) {
            Memory memory = savedReg.get(reg);
            write("\tlw " + reg.getName() + ", " +memory.getOff()+ "($sp)\n");
        }
        savedReg.clear();
    }

//    public void saveRegs() {
//        ArrayList<Reg> usedRegs = new ArrayList<>();
//        for (Reg reg : regT) {
//            if (reg.isUsed()) {
//                usedRegs.add(reg);
//            }
//        }
//        for (Reg reg : usedRegs) {
//            Reg reg1 = gerSavedReg();
//            savedReg.put(reg, reg1);
//            write("\tmove " + reg1.getName() + ", " + reg.getName() + "\n");
//        }
//    }
//
//    public void restoreRegs() {
//        for (Reg reg : savedReg.keySet()) {
//            Reg reg1 = savedReg.get(reg);
//            write("\tmove " + reg.getName() + ", " + reg1.getName() + "\n");
//            reg1.clear();
//        }
//        savedReg.clear();
//    }

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

    public Reg name2Reg(String name) {
        for (Reg reg : regT) {
            if (reg.getName().equals(name))
                return reg;
        }
        for (Reg reg : regS) {
            if (reg.getName().equals(name))
                return reg;
        }
        return null;
    }

    public void allocGp(Value addr, Value value) {
        String name = addr.getName();
        if (symbolTable.containsKey(name)) {
            return;
        }
        symbolTable.put(name, new Memory("$gp", 0, value));
    }

    public void run(IRModule module) throws IOException {
        out = new BufferedWriter(new FileWriter(MipsPath));
        write(".macro GETINT()\nli $v0, 5\nsyscall\n.end_macro\n");
        write("\n.macro PUTINT()\nli $v0, 1\nsyscall\n.end_macro\n");
        write("\n.macro PUTCH()\nli $v0, 11\nsyscall\n.end_macro\n");
        write("\n.macro PUTSTR()\nli $v0, 4\nsyscall\n.end_macro\n");
        write("\n.data\n");
        for (GlobalVar globalVar : module.getGlobalVars()) {
            write("\tg_" + globalVar.toMIPS() + "\n");
            allocGp(globalVar, globalVar);
        }
//        for (int i = 0; i < module.getStringList().size(); i++) {       //todo:没有真用到
//            write("\tstr" + i + ": .asciiz \"" + module.getStringList().get(i) + "\"\n");
//        }
        write("\n.text\n");
        write("\njal main\n");
        write("j return\n");
        initReg();
        for (Function function : module.getFunctions()) {
            varCnt = 0;
            sp = 0;
            ra = sp;
            write("\n" + function.getName() + ":\n");
            store("$ra", "$sp", ra);
            sp -= 4;
            int arg = function.getArgs().size();
            ArrayList<Reg> regs = new ArrayList<>();
            for (int i = 0; i < arg; i++) {
                //todo: 超过4个时存内存
                Reg reg = getFuncReg();
                load(reg.getName(), "$sp", (arg - i) * 4);
                regs.add(reg);
                symbolTable.put(function.getArgs().get(i).getName(), reg);
            }
            for (Reg reg : regs) {
                reg.clear();
            }
            for (BasicBlock bb : function.getBasicBlocks()) {
                write(bb.getName() + ":\n");
                boolean flag = false;
                for (Instruction inst : bb.getInsts()) {
                    if (flag) break;
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
        out.write(sb.toString());
        out.close();
    }

    private void translate(Instruction inst) {
        if (inst instanceof AllocInst) visitAllocInst((AllocInst) inst);
        else if (inst instanceof BinaryInst) visitBinaryInst((BinaryInst) inst);
        else if (inst instanceof BrInst) visitBrInst((BrInst) inst);
        else if (inst instanceof CallInst) visitCallInst((CallInst) inst);
        else if (inst instanceof GEPInst) visitGEPInst((GEPInst) inst);
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
                if (!globalVar.isArray()) {
                    write("\tlw " + reg + ", g_" + globalVar.getName().substring(1) + "_\n");
                } else {
                    if (memory.getOffReg() == null)
                        write("\tlw " + reg + ", g_" + globalVar.getName().substring(1) + "_+" + memory.getOff() + "\n");
                    else {
                        write("\tlw " + reg + ", g_" + globalVar.getName().substring(1) + "_(" + memory.getOffReg().getName() + ")\n");
                        memory.getOffReg().clear();
                    }
                }
            } else {
                if (memory.getOffReg() == null) {
                    write("\tlw " + reg + ", " + memory.getOff() + "(" + memory.getAddr() + ")\n");         //addr:sp或$reg
                    if (name2Reg(memory.getAddr()) != null)
                        name2Reg(memory.getAddr()).clear();                 //为reg时需要释放
                } else {
                    //需要加上sp基地址
                    write("\taddu "+memory.getOffReg().getName()+", "+memory.getOffReg().getName()+", $sp\n");
                    write("\tlw " + reg + ", " + "(" + memory.getOffReg().getName() + ")\n");
                    memory.getOffReg().clear();
                }
            }
        } else if (symbolTable.get(name) instanceof Reg reg0) {
            write("\tmove " + reg + ", " + reg0.getName() + "\n");
            reg0.clear();
        }
    }

    public void store(String reg, String name) {
        Memory memory = (Memory) symbolTable.get(name);
        if (memory.getValue() instanceof GlobalVar globalVar) {
            if (!globalVar.isArray()) {
                write("\tsw " + reg + ", g_" + globalVar.getName().substring(1) + "_\n");
            } else {
                if (memory.getOffReg() == null)
                    write("\tsw " + reg + ", g_" + globalVar.getName().substring(1) + "_+" + memory.getOff() + "\n");
                else {
                    write("\tsw " + reg + ", g_" + globalVar.getName().substring(1) + "_(" + memory.getOffReg().getName() + ")\n");
                    memory.getOffReg().clear();
                }
            }
        } else {
            if (memory.getOffReg() == null) {
                write("\tsw " + reg + ", " + memory.getOff() + "(" + memory.getAddr() + ")\n");
                if (name2Reg(memory.getAddr()) != null) {
                    name2Reg(memory.getAddr()).clear();
                }
            } else {
                write("\taddu "+memory.getOffReg().getName()+", "+memory.getOffReg().getName()+", $sp\n");
                write("\tsw " + reg + ", " + "(" + memory.getOffReg().getName() + ")\n");
                memory.getOffReg().clear();
            }
        }
    }

    //用t0=t0+t1替代
    public void calc(Reg reg0, Reg reg1, Reg reg2, String op) {
        write("\t" + op + " " + reg0.getName() + ", " + reg1.getName() + ", " + reg2.getName() + "\n");
    }

    public void visitAllocInst(AllocInst inst) {
        int cnt = 0;
        Type type = inst.getAllocType();
        if (type instanceof IntType)
            cnt = 1;
        else if (type instanceof ArrayType arrayType) {
            if (arrayType.getElementType() instanceof IntType)
                cnt = arrayType.getSize();
            else if (arrayType.getElementType() instanceof ArrayType arrayType1)
                cnt = arrayType.getSize() * arrayType1.getSize();
        } else if (type instanceof PointerType) {
            cnt = 1;
        }
//        System.out.println("cnt="+cnt);
        varCnt += cnt;
//        symbolTable.put(inst.getName(), new Memory("$sp", sp, new Value(" ", IntType.I32)));
//        sp -= (cnt * 4);
        sp -= (cnt * 4);
        symbolTable.put(inst.getName(), new Memory("$sp", sp + 4, new Value(" ", IntType.I32)));
//        System.out.println("sp="+sp);

    }

    public void visitBinaryInst(BinaryInst inst) {
        if (inst.getOp() == Operator.Zext) {
            Reg reg0 = getReg();
            Reg reg1 = symbol2Reg(inst.getOperand(0).getName());
            write("\tmove " + reg0.getName() + ", " + reg1.getName() + "\n");
            reg1.clear();
            symbolTable.put(inst.getName(), reg0);
        } else {
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
            reg1.clear();
            reg2.clear();
            symbolTable.put(inst.getName(), reg0);
        }
    }

    public void visitBrInst(BrInst inst) {
        if (inst.isJump()) {
            write("\tj " + inst.getJumpBlock().getName() + "\n");
        } else {
            Reg reg = (Reg) symbolTable.get(inst.getBooleanVal().getName());
            write("\tbeq " + reg.getName() + ", $0, " + inst.getFalseBlock().getName() + "\n");
            write("\tj " + inst.getTrueBlock().getName() + "\n");
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

                int spSub = 4 * (size + 1 + varCnt);
                write("\tsubu $sp, $sp, **" + "\n");
                for (int i = 0; i < size; i++) {
                    Value param = inst.getOperands().get(i);
                    if (param.getType() instanceof IntType) {                   //参数为整数
                        Reg reg = symbol2Reg(param.getName());
                        store(reg.getName(), "$sp", (size - i) * 4);
                        reg.clear();
                    }
                    else if (param.getType() instanceof PointerType) {        //参数为数组
                        Memory memory = (Memory) symbolTable.get(param.getName());
                        String addr = memory.getAddr();
                        if (addr.equals("$sp") || addr.equals("$gp")) {
                            Reg reg = getReg();
                            if (addr.equals("$sp")) {       //参数为局部数组
                                Reg offReg = memory.getOffReg();
                                if (offReg == null) {
                                    int off = memory.getOff() + spSub;
                                    write("\taddiu " + reg.getName() + ", $sp, " + off + "\n");
                                } else {
                                    write("\taddiu " + offReg.getName() + ", " + offReg.getName() + ", " + spSub + "\n");
                                    write("\taddu " + reg.getName() + ", $sp, " + offReg.getName() + "\n");
                                    offReg.clear();
                                }
                            } else {                        //参数为全局数组
                                Reg offReg = memory.getOffReg();
                                if (offReg != null) {
                                    write("\tla " + reg.getName() + ", g_" + memory.getValue().getName().substring(1) + "_(" + offReg.getName() + ")\n");
                                    offReg.clear();
                                } else
                                    write("\tla " + reg.getName() + ", g_" + memory.getValue().getName().substring(1) + "_+" + memory.getOff() + "\n");
                            }
                            store(reg.getName(), "$sp", (size - i) * 4);
                            reg.clear();
                        } else {                               //参数为参数
//                            System.out.println(addr);
                            store(addr, "$sp", (size - i) * 4);
                            name2Reg(addr).clear();
                        }
                    }
                }
                //saveRegs
                ArrayList<Reg> usedRegs = new ArrayList<>();
                for (Reg reg : regT) {
                    if (reg.isUsed()) {
                        usedRegs.add(reg);
                    }
                }
                for (Reg reg : regS) {
                    if (reg.isUsed()) {
                        usedRegs.add(reg);
                    }
                }
                int savedRegSize = usedRegs.size();
                spSub += (4*savedRegSize);
                int nowSp = sp + spSub;
                for (Reg reg : usedRegs) {
                    Memory memory = new Memory("$sp", nowSp, new Value("", IntType.I32));
                    savedReg.put(reg, memory);
                    write("\tsw " + reg.getName() + ", " +nowSp+ "($sp)\n");
                    nowSp-=4;
                }

                write("\tjal " + function.getName() + "\n");

//                restoreRegs();
                for (Reg reg : savedReg.keySet()) {
                    Memory memory = savedReg.get(reg);
                    write("\tlw " + reg.getName() + ", " +memory.getOff()+ "($sp)\n");
                }
                savedReg.clear();
                String str = sb.toString();
                str = str.replaceFirst("\\*\\*", String.valueOf(spSub));
                sb = new StringBuilder(str);
                write("\taddu $sp, $sp, " + spSub + "\n");
                //todo:返回值不一定用到会占用寄存器
                if (inst.hasValue()) {
                    Reg reg = getReg();
                    symbolTable.put(inst.getName(), reg);
                    write("\tmove " + reg.getName() + ", $v0\n");
                }
            }
        }
    }

    public void visitGEPInst(GEPInst inst) {
        Value value = inst.getValue();
        Object o = symbolTable.get(value.getName());
        if (o instanceof Memory memory) {               //数组为函数内定义的变量
            int off = -1;           //-1代表出现exception
            ArrayList<Value> indexs = inst.getIndexs();
            int size = indexs.size();
            Reg reg1 = new Reg("meaningless reg error");
            if (size == 2) {
                try {
                    off = Integer.parseInt(indexs.get(1).getName()) * 4;
                } catch (NumberFormatException e) {
                    reg1 = symbol2Reg(indexs.get(1).getName());
                    write("\tsll " + reg1.getName() + ", " + reg1.getName() + ", 2" + "\n");
                }
            } else if (size == 3) {
                Type type = ((PointerType) value.getType()).getpType();
                int arrSize = ((ArrayType) ((ArrayType) (type)).getElementType()).getSize();
                try {
                    off = (Integer.parseInt(indexs.get(1).getName()) * arrSize + Integer.parseInt(indexs.get(2).getName())) * 4;
                } catch (NumberFormatException e) {
                    reg1 = symbol2Reg(indexs.get(1).getName());
                    Reg reg2 = symbol2Reg(indexs.get(2).getName());
                    write("\tmul " + reg1.getName() + ", " + reg1.getName() + ", " + arrSize + "\n");
                    write("\taddu " + reg1.getName() + ", " + reg1.getName() + ", " + reg2.getName() + "\n");
                    write("\tsll " + reg1.getName() + ", " + reg1.getName() + ", 2" + "\n");
                    reg2.clear();
                }
            } else {
                System.out.println("GEP index size error 1");
            }
            if (memory.getValue() instanceof GlobalVar globalVar) {
                if (off != -1)
                    symbolTable.put(inst.getName(), new Memory("$gp", off, globalVar));
                else {
                    symbolTable.put(inst.getName(), new Memory("$gp", reg1, globalVar));
                }

            } else {
//                System.out.println(memory.getOff());
//                System.out.println(off);
                if (off != -1)
                    symbolTable.put(inst.getName(), new Memory("$sp", memory.getOff() + off, new Value("", IntType.I32)));
                else {
                    // 存memory.off, off保存在offReg中
                    write("\taddiu " + reg1.getName() + ", " + reg1.getName() + ", " + memory.getOff() + "\n");
                    symbolTable.put(inst.getName(), new Memory("$sp", reg1, new Value("", IntType.I32)));
                }

            }
        } else if (o instanceof Reg reg) {              //数组为函数的参数
            int off = 0;
            ArrayList<Value> indexs = inst.getIndexs();
            int size = indexs.size();
            if (size == 1) {
                try {
                    off = (Integer.parseInt(indexs.get(0).getName())) * 4;
                } catch (NumberFormatException e) {
                    Reg reg1 = symbol2Reg(indexs.get(0).getName());
                    write("\tsll " + reg1.getName() + ", " + reg1.getName() + ", 2" + "\n");
                    write("\taddu " + reg.getName() + ", " + reg.getName() + ", " + reg1.getName() + "\n");
                    reg1.clear();
                }
            } else if (size == 2) {
                Type type = ((PointerType) value.getType()).getpType();
                int arrSize = ((ArrayType) (type)).getSize();
                try {
                    off = (Integer.parseInt(indexs.get(0).getName()) * arrSize + Integer.parseInt(indexs.get(1).getName())) * 4;
                } catch (NumberFormatException e) {
                    Reg reg1 = symbol2Reg(indexs.get(0).getName());
                    Reg reg2 = symbol2Reg(indexs.get(1).getName());
                    write("\tmul " + reg1.getName() + ", " + reg1.getName() + ", " + arrSize + "\n");
                    write("\taddu " + reg1.getName() + ", " + reg1.getName() + ", " + reg2.getName() + "\n");
                    write("\tsll " + reg1.getName() + ", " + reg1.getName() + ", 2" + "\n");
                    write("\taddu " + reg.getName() + ", " + reg.getName() + ", " + reg1.getName() + "\n");
                    reg1.clear();
                    reg2.clear();
                }
            } else {
                System.out.println("GEP index size error 2");
            }
            //off($reg): Exception时off为0,因为已经给reg加上了相应偏移
//            System.out.println(reg.getName() + off);
            symbolTable.put(inst.getName(), new Memory(reg.getName(), off, new Value("", IntType.I32)));
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
        store(reg.getName(), inst.getPointer().getName());
        reg.clear();
    }

    public void write(String str) {
//        try {
//            out.write(str);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        sb.append(str);
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
