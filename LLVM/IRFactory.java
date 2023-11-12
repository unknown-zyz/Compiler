package LLVM;

import LLVM.Value.*;
import LLVM.Value.Instruction.*;
import LLVM.type.*;

import java.util.ArrayList;

public class IRFactory {
    public GlobalVar buildGlobalVar(String name, Type type, Value value, boolean isConst) {
        return new GlobalVar("@"+name, type, value, isConst);
    }

    public ArrayType buildArrayType(ArrayList<Integer> sizes, Type type) {
        if(sizes.size() == 1){
            return new ArrayType(sizes.get(0), type);
        }

        ArrayList<Integer> newSizes = new ArrayList<>();
        for(int i = 1; i < sizes.size(); i++){
            newSizes.add(sizes.get(i));
        }
        Type newType = buildArrayType(newSizes, type);
        return new ArrayType(sizes.get(0), newType);
    }

    public GlobalVar buildGlobalArray(String name, Type type, ArrayList<Integer> sizes, ArrayList<Value> values) {
        ArrayType arrayType = buildArrayType(sizes, type);
        return new GlobalVar("@"+name, new PointerType(arrayType), values);
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
            case "int*" -> new Argument(name, new PointerType(IntType.I32), function);
            default -> null;
        };
        function.addArg(argument);
        return argument;
    }

    public Argument buildArgument(String name, String type, Function function, ArrayList<Integer> indexs) {
        assert type.equals("int");
        ArrayType arrayType = buildArrayType(indexs, IntType.I32);
        Argument argument = new Argument(name, new PointerType(arrayType), function);
        function.addArg(argument);
        return argument;
    }

    public BasicBlock buildBasicBlock(Function function){
        BasicBlock bb = new BasicBlock(function);
        function.addBasicBlock(bb);
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
        Type type = op.isBoolean() ? IntType.I1 : IntType.I32;
        BinaryInst binaryInst = new BinaryInst(op, left, right, type);
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

    public BrInst buildBrInst(BasicBlock jumpBlock, BasicBlock bb) {
        BrInst brInst = new BrInst(jumpBlock);
        bb.addInst(brInst);
        return brInst;
    }

    public BrInst buildBrInst(Value value, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock bb) {
        BrInst brInst = new BrInst(value, trueBlock, falseBlock);
        bb.addInst(brInst);
        return brInst;
    }

    public GetPtrInst buildGetPtrInst(Value value, ArrayList<Value> indexs, BasicBlock bb) {
        Type type = ((PointerType)value.getType()).getpType();
        for(int i = 1; i < indexs.size();i++)
            type = ((ArrayType)type).getElementType();
        GetPtrInst getPtrInst = new GetPtrInst(value, indexs, new PointerType(type));
        bb.addInst(getPtrInst);
        return getPtrInst;
    }
}
