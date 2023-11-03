package LLVM;

import LLVM.Value.Function;
import LLVM.Value.GlobalVar;

import java.util.ArrayList;

public class IRModule {
    private ArrayList<GlobalVar> globalVars;
    private ArrayList<Function> functions;

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
