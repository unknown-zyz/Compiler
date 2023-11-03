package LLVM;

import LLVM.Value.*;
import LLVM.Value.Instruction.*;
import LLVM.type.IntType;
import LLVM.type.PointerType;
import LLVM.type.Type;
import LLVM.type.VoidType;

import java.util.ArrayList;

public class IRFactory {
    public GlobalVar buildGlobalVar(String name, Type type, Value value, boolean isConst) {
        return new GlobalVar("@"+name, type, value, isConst);
    }

    public ConstInteger buildNumber(String val){
        return new ConstInteger(val, IntType.I32);
    }

    public Function buildFunction(String type, String name, IRModule module) {
        Value.valCnt = -1;
        Function function;
        if(type.equals("int")){
            function = new Function(name, IntType.I32);
        }
        else {
            function = new Function(name, VoidType.voidType);
        }
        module.addFunction(function);
        return function;
    }

    public Argument buildArgument(String name, String type, Function function) {
        Argument argument = switch (type) {
            case "int" -> new Argument(name, IntType.I32, function);
//            case "int*" -> new Argument(name, new PointerType(IntType.I32), function);
            case "void" -> new Argument(name, VoidType.voidType, function);
            default -> null;
        };
        function.addArg(argument);
        return argument;
    }

    public BasicBlock buildBasicBlock(Function function){
        BasicBlock bb = new BasicBlock(function);
        function.setBasicBlock(bb);
        return bb;
    }

    public RetInst buildRetInst(Value value, BasicBlock bb) {
        assert value!=null;
        RetInst retInst = new RetInst(value);
        bb.addInst(retInst);
        return retInst;
    }

    public RetInst buildRetInst(BasicBlock bb) {
        Value voidValue = new Value("", VoidType.voidType);
        RetInst retInst = new RetInst(voidValue);
        bb.addInst(retInst);
        return retInst;
    }

    public BinaryInst buildBinaryInst(Value left, Value right, Operator op, BasicBlock bb) {
        BinaryInst binaryInst = new BinaryInst(op, left, right, IntType.I32);
        bb.addInst(binaryInst);
        return binaryInst;
    }

    public AllocInst buildAllocInst(Type type, BasicBlock bb) {
        AllocInst allocInst = new AllocInst(type);
        bb.addInst(allocInst);
        return allocInst;
    }

    public StoreInst buildStoreInst(Value value, Value pointer, BasicBlock bb) {
        StoreInst storeInst = new StoreInst(value, pointer);
        bb.addInst(storeInst);
        return storeInst;
    }

    public LoadInst buildLoadInst(Value pointer, BasicBlock bb) {
        LoadInst loadInst = new LoadInst(pointer, ((PointerType)pointer.getType()).getpType());
        bb.addInst(loadInst);
        return loadInst;
    }

    public CallInst buildCallInst(Function function, ArrayList<Value> values, BasicBlock bb) {
        CallInst callInst = new CallInst(function, values);
        bb.addInst(callInst);
        return callInst;
    }
}
