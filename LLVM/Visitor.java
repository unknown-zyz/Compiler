package LLVM;

import LLVM.Value.*;
import LLVM.Value.Instruction.AllocInst;
import LLVM.Value.Instruction.BinaryInst;
import LLVM.Value.Instruction.Operator;
import LLVM.type.IntType;
import LLVM.type.PointerType;
import LLVM.type.Type;
import LLVM.type.VoidType;
import Syntax.Node.*;
import Syntax.Node.Number;

import java.util.ArrayList;
import java.util.HashMap;

public class Visitor {
    private IRModule module;
    private Function curFunction;
    private BasicBlock curBB;
    private Value curValue;
    private final ArrayList<GlobalVar> globalVars = new ArrayList<>();
    private final ArrayList<HashMap<String, Value>> symbolTables = new ArrayList<>();
    private final IRFactory f = new IRFactory();

    //符号表
    private void pushSymbol(String str, Value value){
        symbolTables.get(symbolTables.size() - 1).put(str, value);
    }

    private void pushSymbolTable(){
        symbolTables.add(new HashMap<>());
    }

    private void popSymbolTable(){
        symbolTables.remove(symbolTables.size() - 1);
    }

    private Value querySymbol(String str){
        for(int i = symbolTables.size() - 1; i >= 0; i--){
            if(symbolTables.get(i).containsKey(str)){
                return symbolTables.get(i).get(str);
            }
        }
        return switch (str) {
            case "getint" -> func_getint;
            case "putint" -> func_putint;
            case "putch" -> func_putch;
            case "putstr" -> func_putstr;
            default -> null;
        };
    }

    //库函数
    private final Function func_getint = new Function("getint", IntType.I32);
    private final Function func_putint = new Function("putint", VoidType.voidType);
    private final Function func_putch = new Function("putch", VoidType.voidType);
    private final Function func_putstr = new Function("putstr", VoidType.voidType);


    public IRModule visit(CompUnit compUnit) {
        ArrayList<Function> functions = new ArrayList<>();
        module = new IRModule(globalVars, functions);
        initLibFunc();
        pushSymbolTable();
        for(ASTNode node:compUnit.getChild())
        {
            if(node instanceof ConstDecl)
                visitConstDecl((ConstDecl) node, true);
            else if(node instanceof VarDecl)
                visitVarDecl((VarDecl) node, true);
            else if(node instanceof FuncDef)
                visitFuncDef((FuncDef) node);
            else
                visitMainFuncDef((MainFuncDef) node);
        }

//        for (HashMap<String, Value> symbolTable : symbolTables) {
//            symbolTable.forEach((key, value) -> {
//                System.out.println(key + " " + value);
//            });
//        }

        return module;
    }

    public void visitConstDecl(ConstDecl constDecl, boolean isGlobal) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        for(int i = 2; i < constDecl.getChild().size(); i += 2)
            visitConstDef(constDecl.getChild(i, ConstDef.class), IntType.I32, isGlobal);
    }

    public void visitConstDef(ConstDef constDef, Type type, boolean isGlobal) {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Ident ident = constDef.getChild(0, Ident.class);
        String i_name = ident.getWord().getToken();
        int i_dim = ident.getDim();
        if(i_dim>0) {
            visitArray();
        } else {
            if(isGlobal) {
                visitConstInitVal(constDef.getChild(constDef.getChild().size()-1, ConstInitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type), new Value(String.valueOf(GlobalInt), IntType.I32), true);
                globalVars.add((GlobalVar) curValue);
            } else {
                curValue = f.buildAllocInst(new PointerType(type), curBB);
                if(constDef.getChild(constDef.getChild().size()-1, ConstInitVal.class) != null)
                {
                    Value tmpValue = curValue;
                    visitConstInitVal(constDef.getChild(constDef.getChild().size()-1, ConstInitVal.class), false);
                    f.buildStoreInst(curValue, tmpValue, curBB);
                    curValue = tmpValue;
                }
            }
            pushSymbol(i_name, curValue);
        }
    }

    public void visitConstInitVal(ConstInitVal constInitVal, boolean isGlobal) {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if(constInitVal.getChild(0, Terminal.class) != null) {
            // '{'
            for(int i=1;i<constInitVal.getChild().size();i+=2)
                visitConstInitVal(constInitVal.getChild(i, ConstInitVal.class), isGlobal);
        } else {
            // ConstExp
            visitConstExp(constInitVal.getChild(0, ConstExp.class), isGlobal);
        }
    }

    public void visitVarDecl(VarDecl varDecl, boolean isGlobal) {
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        for(int i = 1; i < varDecl.getChild().size(); i += 2)
        {
            GlobalInt = 0;
            visitVarDef(varDecl.getChild(i, VarDef.class), IntType.I32, isGlobal);
        }

    }

    public void visitVarDef(VarDef varDef, Type type, boolean isGlobal) {
        //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        Ident ident = varDef.getChild(0, Ident.class);
        String i_name = ident.getWord().getToken();
        int i_dim = ident.getDim();
        if(i_dim>0) {
            visitArray();
        } else {
            if(isGlobal) {
                if(varDef.getChild(varDef.getChild().size()-1, InitVal.class) != null)
                    visitInitVal(varDef.getChild(varDef.getChild().size()-1, InitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type),  new Value(String.valueOf(GlobalInt), IntType.I32), false);
                globalVars.add((GlobalVar) curValue);
            } else {
                curValue = f.buildAllocInst(new PointerType(type), curBB);
                if(varDef.getChild(varDef.getChild().size()-1, InitVal.class) != null)
                {
                    Value tmpValue = curValue;
                    visitInitVal(varDef.getChild(varDef.getChild().size()-1, InitVal.class), false);
                    f.buildStoreInst(curValue, tmpValue, curBB);
                    curValue = tmpValue;
                }
            }
            pushSymbol(i_name, curValue);
        }
    }

    public void visitInitVal(InitVal initVal, boolean isGlobal) {
        //  InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if(initVal.getChild(0, Terminal.class) != null) {
            // '{' [ InitVal { ',' InitVal } ] '}'
            for(int i=1;i<initVal.getChild().size();i+=2)
                visitInitVal(initVal.getChild(i, InitVal.class), isGlobal);
        } else {
            // Exp
            visitExp(initVal.getChild(0, Exp.class), isGlobal);
        }
    }

    public void visitArray() {

    }

    public void visitFuncDef(FuncDef funcDef) {
        //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        String type = funcDef.getChild(0, FuncType.class).getChild(0);
        String name = funcDef.getChild(1);
        curFunction = f.buildFunction(type, name, module);
        pushSymbol(name, curFunction);
        curBB = f.buildBasicBlock(curFunction);
        pushSymbolTable();
        if(funcDef.getChild(3, FuncFParams.class) != null)
            visitFuncFParams(funcDef.getChild(3, FuncFParams.class));
        visitBlock(funcDef.getChild(funcDef.getChild().size() - 1, Block.class));
        popSymbolTable();
    }

    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        String type = mainFuncDef.getChild(0);
        String name = mainFuncDef.getChild(1);
        curFunction = f.buildFunction(type, name, module);
        pushSymbol(name, curFunction);
        curBB = f.buildBasicBlock(curFunction);
//        curValue = new Value("0", IntType.I32);           //???
        pushSymbolTable();
        visitBlock(mainFuncDef.getChild(4, Block.class));
        popSymbolTable();
    }

    public void visitFuncFParams(FuncFParams funcFParams) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        for(int i = 0; i < funcFParams.getChild().size(); i += 2)
            visitFuncFParam(funcFParams.getChild(i, FuncFParam.class));
    }

    public void visitFuncFParam(FuncFParam funcFParam) {
        //  FuncFParam → BType Ident [ '[' ']' { '[' ConstExp ']' } ]
        String type = funcFParam.getChild(0);
        String name = funcFParam.getChild(1);
        if(funcFParam.getChild().size()==2)
        {
            Argument argument = f.buildArgument(name, type, curFunction);
            AllocInst allocInst = f.buildAllocInst(new PointerType(argument.getType()), curBB);
            f.buildStoreInst(argument, allocInst, curBB);
            pushSymbol(name, allocInst);
        }
        else
        {
            visitArray();
        }
    }

    public void visitBlock(Block block) {
        for(int i = 1; i< block.getChild().size()-1; i++)
        {
            ASTNode node = block.getChild().get(i);
            if(node instanceof ConstDecl constDecl)
                visitConstDecl(constDecl, false);
            else if(node instanceof VarDecl varDecl)
                visitVarDecl(varDecl, false);
            else if(node instanceof Stmt stmt)
                visitStmt(stmt);
        }
    }

    public void visitStmt(Stmt stmt) {
        if(stmt.getChild(0, LVal.class) != null) {
            if(stmt.getChild(3).equals(";")) {              // LVal '=' Exp ';'
                visitLVal(stmt.getChild(0, LVal.class), true);
                Value tmpValue = curValue;
                visitExp(stmt.getChild(2, Exp.class), false);
                f.buildStoreInst(curValue, tmpValue, curBB);
            } else if(stmt.getChild(3).equals("(")) {       // LVal '=' 'getint' '(' ')' ';'
                visitLVal(stmt.getChild(0, LVal.class), true);
                Value tmpValue = curValue;
                curValue = f.buildCallInst(func_getint, new ArrayList<>(), curBB);
                f.buildStoreInst(curValue, tmpValue, curBB);
            }
        } else if(stmt.getChild(0, Exp.class) != null) {
            visitExp(stmt.getChild(0, Exp.class), false);
        }
        else if(stmt.getChild(0, Block.class) != null) {
            pushSymbolTable();
            visitBlock(stmt.getChild(0, Block.class));
            popSymbolTable();
        } else {
            String str = stmt.getChild(0);
            if(str.equals(";")) {

            } else if(str.equals("if")) {

            } else if(str.equals("for")) {

            } else if(str.equals("break")) {

            } else if(str.equals("continue")) {

            } else if(str.equals("return")) {
                if(stmt.getChild(1, Exp.class)!=null)
                {
                    visitExp(stmt.getChild(1, Exp.class), false);
                    f.buildRetInst(curValue, curBB);
                }
                else f.buildRetInst(curBB);
            } else if(str.equals("printf")) {
                String formatStr = stmt.getChild(2);
                int cnt = 2;
                for(int i = 1;i < formatStr.length()-1; i++) {
                    if(formatStr.charAt(i) == '%') {
                        if(formatStr.charAt(i+1) == 'd') {
                            visitExp(stmt.getChild(2 + cnt, Exp.class), false);
                            cnt+=2;
                            f.buildCallInst(func_putint, new ArrayList<>(){{add(curValue);}}, curBB);
                            i++;
                        }
                    } else if(formatStr.charAt(i) == '\\') {
                        if(formatStr.charAt(i+1) == 'n') {
                            f.buildCallInst(func_putch, new ArrayList<>(){{add(f.buildNumber("10"));}}, curBB);
                            i++;
                        }
                    } else {
                        int finalI = i;
                        f.buildCallInst(func_putch, new ArrayList<>(){{add(new Value(String.valueOf((int)formatStr.charAt(finalI)), IntType.I32));}}, curBB);
                    }
                }
            }
        }
    }

    public void visitConstExp(ConstExp constExp, boolean isGlobal) {
        if(isGlobal)
        {
            GlobalInt = visitGlobalAddExp(constExp.getChild(0, AddExp.class));
//            getExpValue(constExp.getChild(0, AddExp.class));
        }
        else
            visitAddExp(constExp.getChild(0, AddExp.class));
    }

    public void visitExp(Exp exp, boolean isGlobal) {
        if(isGlobal)
            GlobalInt = visitGlobalAddExp(exp.getChild(0, AddExp.class));
        else
            visitAddExp(exp.getChild(0, AddExp.class));
    }

    public void visitLVal(LVal lVal, boolean isAssign) {
        // LVal → Ident {'[' Exp ']'}
        Ident ident = lVal.getChild(0, Ident.class);
        curValue = querySymbol(ident.getWord().getToken());
        assert curValue != null;
        if(!isAssign) curValue = f.buildLoadInst(curValue, curBB);
        //Array   Const?


    }

    public void visitPrimaryExp(PrimaryExp primaryExp) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if(primaryExp.getChild(0, Terminal.class) != null) {
            visitExp(primaryExp.getChild(1, Exp.class), false);
        } else if(primaryExp.getChild(0, LVal.class) != null) {
            visitLVal(primaryExp.getChild(0, LVal.class), false);
        } else {
            visitNumber(primaryExp.getChild(0, Number.class));
        }

    }

    public void visitNumber(Number number) {
        curValue = f.buildNumber(number.getChild(0));
    }

    public void visitUnaryExp(UnaryExp unaryExp) {
        //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        ASTNode node = unaryExp.getChild().get(0);
        if(node instanceof UnaryOp) {
            String str = unaryExp.getChild(0, UnaryOp.class).getChild(0);
            if(str.equals("+")) {
                visitUnaryExp(unaryExp.getChild(1, UnaryExp.class));
            } else if(str.equals("-")) {
                visitUnaryExp(unaryExp.getChild(1, UnaryExp.class));
                curValue = f.buildBinaryInst(f.buildNumber("0"), curValue, Operator.Sub, curBB);
            } else if(str.equals("!")){

            }
        } else if(node instanceof Ident) {
            String ident = unaryExp.getChild(0);
            Function function = (Function) querySymbol(ident);
            ArrayList<Value> values = new ArrayList<>();
            if(unaryExp.getChild(2, FuncRParams.class) != null)
            {
                FuncRParams funcRParams = unaryExp.getChild(2, FuncRParams.class);
                for(int i = 0; i < funcRParams.getChild().size(); i += 2) {
                    visitExp(funcRParams.getChild(i, Exp.class), false);
                    values.add(curValue);
                }
            }
            curValue = f.buildCallInst(function, values, curBB);
        } else
            visitPrimaryExp(unaryExp.getChild(0, PrimaryExp.class));
    }

    public void visitFuncRParams(FuncRParams funcRParams) {
        for(int i = 0; i < funcRParams.getChild().size(); i += 2) {
            visitExp(funcRParams.getChild(i, Exp.class), false);
        }
    }

    public void visitMulExp(MulExp mulExp) {
        ASTNode node = mulExp.getChild().get(0);
        if(node instanceof UnaryExp)
            visitUnaryExp(mulExp.getChild(0, UnaryExp.class));
        else {
            visitMulExp(mulExp.getChild(0, MulExp.class));
            for(int i = 2; i < mulExp.getChild().size()-2; i += 2) {
                Value tmpValue = curValue;
                visitMulExp(mulExp.getChild(i, MulExp.class));
                Operator op = switch (mulExp.getChild(i-1)) {
                    case "*" -> Operator.Mul;
                    case "/" -> Operator.Div;
                    case "%" -> Operator.Mod;
                    default -> throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(i-1));
                };
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitUnaryExp(mulExp.getChild(mulExp.getChild().size()-1, UnaryExp.class));
            Operator op = switch (mulExp.getChild(mulExp.getChild().size()-2)) {
                case "*" -> Operator.Mul;
                case "/" -> Operator.Div;
                case "%" -> Operator.Mod;
                default -> throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(mulExp.getChild().size()-2));
            };
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void visitAddExp(AddExp addExp) {
        ASTNode node = addExp.getChild().get(0);
        if(node instanceof MulExp)
            visitMulExp(addExp.getChild(0, MulExp.class));
        else {
            visitAddExp(addExp.getChild(0, AddExp.class));
            for(int i = 2; i < addExp.getChild().size()-2; i += 2) {
                Value tmpValue = curValue;
                visitAddExp(addExp.getChild(i, AddExp.class));
                Operator op = addExp.getChild(i-1).equals("+") ? Operator.Add : Operator.Sub;
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitMulExp(addExp.getChild(addExp.getChild().size()-1, MulExp.class));
            Operator op = addExp.getChild(addExp.getChild().size()-2).equals("+") ? Operator.Add : Operator.Sub;
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void initLibFunc() {
        func_putint.addArg(new Argument("x", IntType.I32, func_putint));
        func_putch.addArg(new Argument("c", IntType.I32, func_putch));
        func_putstr.addArg(new Argument("str", new PointerType(IntType.I8), func_putstr));
    }

    private int GlobalInt = 0;
    public int visitGlobalAddExp(AddExp addExp) {
        ASTNode node = addExp.getChild().get(0);
        if(node instanceof MulExp)
            return visitGlobalMulExp(addExp.getChild(0, MulExp.class));
        else {
            int x, y;
            x = visitGlobalAddExp(addExp.getChild(0, AddExp.class));
            for(int i = 2; i < addExp.getChild().size()-2; i += 2) {
                y = visitGlobalAddExp(addExp.getChild(i, AddExp.class));
                Operator op = addExp.getChild(i-1).equals("+") ? Operator.Add : Operator.Sub;
                x = cal(x, y, op);
            }
            y = visitGlobalMulExp(addExp.getChild(addExp.getChild().size()-1, MulExp.class));
            Operator op = addExp.getChild(addExp.getChild().size()-2).equals("+") ? Operator.Add : Operator.Sub;
            x = cal(x, y, op);
            return x;
        }
    }

    public int visitGlobalMulExp(MulExp mulExp) {
        ASTNode node = mulExp.getChild().get(0);
        if(node instanceof UnaryExp)
            return visitGlobalUnaryExp(mulExp.getChild(0, UnaryExp.class));
        else {
            int x, y;
            x = visitGlobalMulExp(mulExp.getChild(0, MulExp.class));
            for(int i = 2; i < mulExp.getChild().size()-2; i += 2) {
                y = visitGlobalMulExp(mulExp.getChild(i, MulExp.class));
                Operator op = switch (mulExp.getChild(i-1)) {
                    case "*" -> Operator.Mul;
                    case "/" -> Operator.Div;
                    case "%" -> Operator.Mod;
                    default -> throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(i-1));
                };
                x = cal(x, y, op);
            }
            y = visitGlobalUnaryExp(mulExp.getChild(mulExp.getChild().size()-1, UnaryExp.class));
            Operator op = switch (mulExp.getChild(mulExp.getChild().size()-2)) {
                case "*" -> Operator.Mul;
                case "/" -> Operator.Div;
                case "%" -> Operator.Mod;
                default -> throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(mulExp.getChild().size()-2));
            };
            x = cal(x, y, op);
            return x;
        }
    }

    public int visitGlobalUnaryExp(UnaryExp unaryExp) {
        ASTNode node = unaryExp.getChild().get(0);
        if(node instanceof UnaryOp) {
            String str = unaryExp.getChild(0, UnaryOp.class).getChild(0);
            if(str.equals("+")) {
                return visitGlobalUnaryExp(unaryExp.getChild(1, UnaryExp.class));
            } else {
                return -visitGlobalUnaryExp(unaryExp.getChild(1, UnaryExp.class));
            }
        } else
            return visitGlobalPrimaryExp(unaryExp.getChild(0, PrimaryExp.class));
    }

    public int visitGlobalPrimaryExp(PrimaryExp primaryExp) {
        if(primaryExp.getChild(0, Terminal.class) != null) {
            return visitGlobalAddExp(primaryExp.getChild(1, Exp.class).getChild(0,AddExp.class));
        } else if(primaryExp.getChild(0, LVal.class) != null) {
            String name = primaryExp.getChild(0, LVal.class).getChild(0);
            for(GlobalVar globalVar:globalVars)
            {
                if(globalVar.getName().equals("@"+name))
                    return Integer.parseInt(globalVar.getValue().getName());
            }
            System.out.println("Error: " + name + " not found!");
            return 0;
        } else {
            return Integer.parseInt(primaryExp.getChild(0, Number.class).getChild(0));
        }
    }

    public int cal(int x, int y, Operator op) {
        return switch (op) {
            case Add -> x + y;
            case Sub -> x - y;
            case Mul -> x * y;
            case Div -> x / y;
            case Mod -> x % y;
            default -> 0;
        };
    }
}
