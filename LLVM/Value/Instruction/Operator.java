package LLVM.Value.Instruction;

public enum Operator {
    Add,
    Fadd,
    Sub,
    Fsub,
    Mul,
    Fmul,
    Div,
    Fdiv,
    Mod,
    Fmod,
    Shl,
    Shr,
    And,
    Or,
    Xor,
    Lt,
    FLt,
    Le,
    FLe,
    Ge,
    FGe,
    Gt,
    FGt,
    Eq,
    FEq,
    Ne,
    FNe,
    //conversion op
    Zext,
    Ftoi,
    Itof,
    //Mem
    Alloca,
    Load,
    Store,
    GEP, //get element ptr
    Phi,
    //terminator op
    Br,
    Call,
    Ret,
    //not op
    Not,
    Move,
    BitCast;

    public boolean isCmpOP(){
        String name = name();
        return switch (name) {
            case "Ne", "FNe", "Eq", "FEq", "Lt", "Le", "FLt", "Gt", "FLe", "FGt", "Ge", "FGe" -> true;
            default -> false;
        };
    }

    public boolean isFloat(){
        String name = name();
        return switch (name) {
            case "Fadd", "FNe", "Fsub", "FEq", "Fmul", "Fdiv", "FLt", "Fmod", "FLe", "FGt", "FGe" -> true;
            default -> false;
        };
    }

    public static Operator turnToFloat(Operator op){
        return switch (op){
            case Add, Fadd -> Operator.Fadd;
            case Sub, Fsub -> Operator.Fsub;
            case Mul, Fmul -> Operator.Fmul;
            case Div, Fdiv -> Operator.Fdiv;
            case Mod, Fmod -> Operator.Fmod;
            case Lt, FLt -> Operator.FLt;
            case Le, FLe -> Operator.FLe;
            case Ge, FGe -> Operator.FGe;
            case Gt, FGt -> Operator.FGt;
            case Eq, FEq -> Operator.FEq;
            case Ne, FNe -> Operator.FNe;
            default -> op;
        };
    }

    public static Operator str2op(String str){
        return switch (str){
            case "+" -> Operator.Add;
            case "+f" -> Operator.Fadd;
            case "-" -> Operator.Sub;
            case "-f" -> Operator.Fsub;
            case "*" -> Operator.Mul;
            case "*f" -> Operator.Fmul;
            case "/" -> Operator.Div;
            case "/f" -> Operator.Fdiv;
            case "%" -> Operator.Mod;
            case "%f" -> Operator.Fmod;
            case "<=" -> Operator.Le;
            case "<=f" -> Operator.FLe;
            case "<" -> Operator.Lt;
            case "<f" -> Operator.FLt;
            case ">=" -> Operator.Ge;
            case ">=f" -> Operator.FGe;
            case ">" -> Operator.Gt;
            case ">f" -> Operator.FGt;
            case "==" -> Operator.Eq;
            case "==f" -> Operator.FEq;
            case "!=" -> Operator.Ne;
            case "!=f" -> Operator.FNe;
            case "&&" -> Operator.And;
            case "||" -> Operator.Or;
            case "ftoi" -> Operator.Ftoi;
            case "itof" -> Operator.Itof;
            case "zext" -> Operator.Zext;
            case "bitcast" -> Operator.BitCast;
            default -> null;
        };
    }

    @Override
    public String toString(){
        String name = name();
        return switch (name) {
            case "Add" -> "add";
            case "Fadd" -> "fadd";
            case "Sub" -> "sub";
            case "Fsub" -> "fsub";
            case "Mul" -> "mul";
            case "Fmul" -> "fmul";
            case "Div" -> "sdiv";
            case "Fdiv" -> "fdiv";
            case "Mod" -> "srem";
            case "Fmod" -> "frem";
            case "Ne" -> "icmp ne";
            case "FNe" -> "fcmp one";
            case "Eq" -> "icmp eq";
            case "FEq" -> "fcmp oeq";
            case "Lt" -> "icmp slt";
            case "FLt" -> "fcmp olt";
            case "Le" -> "icmp sle";
            case "FLe" -> "fcmp ole";
            case "Gt" -> "icmp sgt";
            case "FGt" -> "fcmp ogt";
            case "Ge" -> "icmp sge";
            case "FGe" -> "fcmp oge";

            case "Ftoi" -> "fptosi";
            case "Itof" -> "sitofp";
            case "Zext" -> "zext";
            case "BitCast" -> "bitcast";
            case "Move" -> "move";
            default -> null;
        };
    }
}


