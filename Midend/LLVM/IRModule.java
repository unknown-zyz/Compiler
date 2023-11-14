package Midend.LLVM;

import Midend.LLVM.Value.Function;
import Midend.LLVM.Value.GlobalVar;

import java.util.ArrayList;

public class IRModule {
    private final ArrayList<GlobalVar> globalVars;
    private final ArrayList<Function> functions;

    public IRModule(ArrayList<GlobalVar> globalVars, ArrayList<Function> functions) {
        this.globalVars = globalVars;
        this.functions = functions;
    }

    public void addGlobalVar(GlobalVar globalVar){
        globalVars.add(globalVar);
    }

    public void addFunction(Function function){
        functions.add(function);
    }

    public ArrayList<GlobalVar> getGlobalVars() {
        return globalVars;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }
}
