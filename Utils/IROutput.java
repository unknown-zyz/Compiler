package Utils;

import LLVM.IRModule;
import LLVM.Value.Argument;
import LLVM.Value.BasicBlock;
import LLVM.Value.Function;
import LLVM.Value.GlobalVar;
import LLVM.Value.Instruction.Instruction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IROutput {
    private static BufferedWriter out;

    private static void GlobalVarOutput(GlobalVar globalVar) throws IOException {
        out.write(globalVar.getName());
        out.write(" = ");
        if(globalVar.isConst())
            out.write("constant ");
        else
            out.write("global ");
        out.write(globalVar.getValue().toString());
    }

    private static void LibFuncOutput() throws IOException {
        out.write("declare i32 @getint()\n");
        out.write("declare void @putint(i32)\n");
        out.write("declare void @putch(i32)\n");
        out.write("declare void @putstr(i8*)\n");
    }
    private static void FunctionOutput(Function function) throws IOException {
        out.write("define dso_local ");
        if(function.getType().isInteger())
            out.write("i32 ");
        else
            out.write("void ");
        out.write("@"+function.getName()+"(");
        ArrayList<Argument> args = function.getArgs();
        for(Argument argument:args)
        {
            out.write(argument.toString());
            if(args.indexOf(argument)!=args.size()-1)
                out.write(", ");
        }
        out.write("){\n");
        for(BasicBlock bb:function.getBasicBlock())
            BasicBlockOutput(bb);
        out.write("}\n");
    }

    private static void refactorFunction(Function function) {
        int cnt = -1;
        ArrayList<Argument> args = function.getArgs();
        for(Argument arg : args){
            arg.setName("%" + ++cnt);
        }
        for(BasicBlock bb:function.getBasicBlock())
        {
            for(Instruction inst: bb.getInsts()) {
                if (inst.hasValue())
                {
                    inst.setName("%" + ++cnt);
                }
            }
        }
    }

    private static void BasicBlockOutput(BasicBlock basicBlock) throws IOException {
        out.write(basicBlock.getName()+":\n");
        for(Instruction inst:basicBlock.getInsts())
        {
            out.write("\t");
            InstOutput(inst);
        }
    }

    private static void InstOutput(Instruction inst) throws IOException {
        out.write(inst.toString());
        out.write("\n");
    }

    public static void ModuleOutput(IRModule module, String filename) throws IOException {
        out = new BufferedWriter(new FileWriter(filename));
        LibFuncOutput();
        out.write("\n");
        ArrayList<GlobalVar> globalVars = module.getGlobalVars();
        ArrayList<Function> functions = module.getFunctions();
        for(GlobalVar globalVar:globalVars)
        {
            GlobalVarOutput(globalVar);
            out.write("\n");
        }
        out.write("\n");
        for (Function function : functions) {
            refactorFunction(function);
            FunctionOutput(function);
            out.write("\n");
        }
        out.close();
    }
}
