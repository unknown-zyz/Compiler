package LLVM.Value.Instruction;

import LLVM.Value.BasicBlock;
import LLVM.Value.Value;
import LLVM.type.VoidType;

public class BrInst extends Instruction{
    private int type;
    private BasicBlock trueBlock;
    private BasicBlock falseBlock;
    private BasicBlock jumpBlock;

    public BrInst(BasicBlock jumpBlock) {
        super("", VoidType.voidType, Operator.Br);
        this.jumpBlock = jumpBlock;
        this.type = 1;
    }

    public BrInst(Value value, BasicBlock trueBlock, BasicBlock falseBlock){
        super("", VoidType.voidType, Operator.Br);
        addOperand(value);
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
        this.type = 2;
    }

    public Value getBooleanVal(){
        return getOperand(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(type == 1)
        {
            sb.append("br label %");
            sb.append(jumpBlock.getName());
        }
        else {
            sb.append("br ");
            sb.append(getBooleanVal().getType()).append(" ").append(getBooleanVal().getName()).append(", ");
            sb.append("label %").append(trueBlock.getName()).append(", ");
            sb.append("label %").append(falseBlock.getName());
        }
        return sb.toString();
    }

    @Override
    public boolean hasValue() {
        return false;
    }
}
