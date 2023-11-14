package Midend.LLVM.Value.Instruction;

public enum Operator {
    Add,
    Sub,
    Mul,
    Div,
    Mod,
    And,
    Or,
    Lt,
    Le,
    Ge,
    Gt,
    Eq,
    Ne,
    Alloca,
    Load,
    Store,
    GEP,
    Phi,
    Br,
    Call,
    Ret,
    Zext;

    public boolean isBoolean(){
        String name = name();
        return switch (name) {
            case "Ne", "Eq",  "Lt", "Le", "Gt", "Ge"  -> true;
            default -> false;
        };
    }


    public static Operator str2op(String str){
        return switch (str){
            case "+" -> Operator.Add;
            case "-" -> Operator.Sub;
            case "*" -> Operator.Mul;
            case "/" -> Operator.Div;
            case "%" -> Operator.Mod;
            case "<=" -> Operator.Le;
            case "<" -> Operator.Lt;
            case ">=" -> Operator.Ge;
            case ">" -> Operator.Gt;
            case "==" -> Operator.Eq;
            case "!=" -> Operator.Ne;
            case "&&" -> Operator.And;
            case "||" -> Operator.Or;
            case "zext" -> Operator.Zext;
            default -> null;
        };
    }

    @Override
    public String toString(){
        String name = name();
        return switch (name) {
            case "Add" -> "add";
            case "Sub" -> "sub";
            case "Mul" -> "mul";
            case "Div" -> "sdiv";
            case "Mod" -> "srem";
            case "Ne" -> "icmp ne";
            case "Eq" -> "icmp eq";
            case "Lt" -> "icmp slt";
            case "Le" -> "icmp sle";
            case "Gt" -> "icmp sgt";
            case "Ge" -> "icmp sge";
            case "Zext" -> "zext";
            default -> null;
        };
    }
}


