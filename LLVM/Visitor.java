package LLVM;

import LLVM.Value.*;
import LLVM.Value.Instruction.AllocInst;
import LLVM.Value.Instruction.Operator;
import LLVM.type.IntType;
import LLVM.type.PointerType;
import LLVM.type.Type;
import LLVM.type.VoidType;
import Lexical.TokenType;
import Lexical.Word;
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
    private int GlobalInt = 0;
    private BasicBlock forBB;
    private BasicBlock forNextBB;

    //符号表
    private void pushSymbol(String str, Value value) {
        symbolTables.get(symbolTables.size() - 1).put(str, value);
    }

    private void pushSymbolTable() {
        symbolTables.add(new HashMap<>());
    }

    private void popSymbolTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    private Value querySymbol(String str) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).containsKey(str)) {
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
        for (ASTNode node : compUnit.getChild()) {
            if (node instanceof ConstDecl)
                visitConstDecl((ConstDecl) node, true);
            else if (node instanceof VarDecl)
                visitVarDecl((VarDecl) node, true);
            else if (node instanceof FuncDef)
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
        for (int i = 2; i < constDecl.getChild().size(); i += 2)
            visitConstDef(constDecl.getChild(i, ConstDef.class), IntType.I32, isGlobal);
    }

    public void visitConstDef(ConstDef constDef, Type type, boolean isGlobal) {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Ident ident = constDef.getChild(0, Ident.class);
        String i_name = ident.getWord().getToken();
        int i_dim = ident.getDim();
        if (i_dim > 0) {
            visitArray();
        } else {
            if (isGlobal) {
                visitConstInitVal(constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type), new Value(String.valueOf(GlobalInt), IntType.I32), true);
                globalVars.add((GlobalVar) curValue);
            } else {
                curValue = f.buildAllocInst(new PointerType(type), curBB);
                if (constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class) != null) {
                    Value tmpValue = curValue;
                    visitConstInitVal(constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class), false);
                    f.buildStoreInst(curValue, tmpValue, curBB);
                    curValue = tmpValue;
                }
            }
            pushSymbol(i_name, curValue);
        }
    }

    public void visitConstInitVal(ConstInitVal constInitVal, boolean isGlobal) {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (constInitVal.getChild(0, Terminal.class) != null) {
            // '{'
            for (int i = 1; i < constInitVal.getChild().size(); i += 2)
                visitConstInitVal(constInitVal.getChild(i, ConstInitVal.class), isGlobal);
        } else {
            // ConstExp
            visitConstExp(constInitVal.getChild(0, ConstExp.class), isGlobal);
        }
    }

    public void visitVarDecl(VarDecl varDecl, boolean isGlobal) {
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        for (int i = 1; i < varDecl.getChild().size(); i += 2) {
            GlobalInt = 0;
            visitVarDef(varDecl.getChild(i, VarDef.class), IntType.I32, isGlobal);
        }

    }

    public void visitVarDef(VarDef varDef, Type type, boolean isGlobal) {
        //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        Ident ident = varDef.getChild(0, Ident.class);
        String i_name = ident.getWord().getToken();
        int i_dim = ident.getDim();
        if (i_dim > 0) {
            visitArray();
        } else {
            if (isGlobal) {
                if (varDef.getChild(varDef.getChild().size() - 1, InitVal.class) != null)
                    visitInitVal(varDef.getChild(varDef.getChild().size() - 1, InitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type), new Value(String.valueOf(GlobalInt), IntType.I32), false);
                globalVars.add((GlobalVar) curValue);
            } else {
                curValue = f.buildAllocInst(new PointerType(type), curBB);
                if (varDef.getChild(varDef.getChild().size() - 1, InitVal.class) != null) {
                    Value tmpValue = curValue;
                    visitInitVal(varDef.getChild(varDef.getChild().size() - 1, InitVal.class), false);
                    f.buildStoreInst(curValue, tmpValue, curBB);
                    curValue = tmpValue;
                }
            }
            pushSymbol(i_name, curValue);
        }
    }

    public void visitInitVal(InitVal initVal, boolean isGlobal) {
        //  InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if (initVal.getChild(0, Terminal.class) != null) {
            // '{' [ InitVal { ',' InitVal } ] '}'
            for (int i = 1; i < initVal.getChild().size(); i += 2)
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
        if (funcDef.getChild(3, FuncFParams.class) != null)
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
        for (int i = 0; i < funcFParams.getChild().size(); i += 2)
            visitFuncFParam(funcFParams.getChild(i, FuncFParam.class));
    }

    public void visitFuncFParam(FuncFParam funcFParam) {
        //  FuncFParam → BType Ident [ '[' ']' { '[' ConstExp ']' } ]
        String type = funcFParam.getChild(0);
        String name = funcFParam.getChild(1);
        if (funcFParam.getChild().size() == 2) {
            Argument argument = f.buildArgument(name, type, curFunction);
            AllocInst allocInst = f.buildAllocInst(new PointerType(argument.getType()), curBB);
            f.buildStoreInst(argument, allocInst, curBB);
            pushSymbol(name, allocInst);
        } else {
            visitArray();
        }
    }

    public void visitBlock(Block block) {
        for (int i = 1; i < block.getChild().size() - 1; i++) {
            ASTNode node = block.getChild().get(i);
            if (node instanceof ConstDecl constDecl)
                visitConstDecl(constDecl, false);
            else if (node instanceof VarDecl varDecl)
                visitVarDecl(varDecl, false);
            else if (node instanceof Stmt stmt)
                visitStmt(stmt);
        }
    }

    public void visitStmt(Stmt stmt) {
        if (stmt.getChild(0, LVal.class) != null) {
            if (stmt.getChild(3).equals(";")) {              // LVal '=' Exp ';'
                visitLVal(stmt.getChild(0, LVal.class), true);
                Value tmpValue = curValue;
                visitExp(stmt.getChild(2, Exp.class), false);
                f.buildStoreInst(curValue, tmpValue, curBB);
            } else if (stmt.getChild(3).equals("(")) {       // LVal '=' 'getint' '(' ')' ';'
                visitLVal(stmt.getChild(0, LVal.class), true);
                Value tmpValue = curValue;
                curValue = f.buildCallInst(func_getint, new ArrayList<>(), curBB);
                f.buildStoreInst(curValue, tmpValue, curBB);
            }
        } else if (stmt.getChild(0, Exp.class) != null) {
            visitExp(stmt.getChild(0, Exp.class), false);
        } else if (stmt.getChild(0, Block.class) != null) {
            pushSymbolTable();
            visitBlock(stmt.getChild(0, Block.class));
            popSymbolTable();
        } else {
            String str = stmt.getChild(0);
            if (str.equals(";")) {

            } else if (str.equals("if")) {
                BasicBlock trueBlock = f.buildBasicBlock(curFunction);
                BasicBlock nextBlock = f.buildBasicBlock(curFunction);
                BasicBlock falseBlock = null;
                boolean elseFlag = (stmt.getChild().size() > 5);
                if (elseFlag) {
                    falseBlock = f.buildBasicBlock(curFunction);
                    visitCond(stmt.getChild(2, Cond.class), trueBlock, falseBlock);
                } else
                    visitCond(stmt.getChild(2, Cond.class), trueBlock, nextBlock);
                curBB = trueBlock;
                visitStmt(stmt.getChild(4, Stmt.class));
                f.buildBrInst(nextBlock, curBB);
                if (elseFlag) {
                    curBB = falseBlock;
                    visitStmt(stmt.getChild(6, Stmt.class));
                    f.buildBrInst(nextBlock, curBB);
                }
                curBB = nextBlock;
            } else if (str.equals("for")) {
                ForStmt forStmt1 = null, forStmt2 = null;
                Cond cond = null;
                int flag = 0;   //记录;数量来判断位置
                for (int i = 2; i < stmt.getChild().size() - 2; i++) {
                    if(stmt.getChild(i) != null && stmt.getChild(i).equals(";")) {
                        flag++;
                    }
                    else if(stmt.getChild(i, ForStmt.class)!=null && flag == 0) {
                        forStmt1 = stmt.getChild(i, ForStmt.class);
                    }
                    else if(stmt.getChild(i, Cond.class)!=null && flag == 1) {
                        cond = stmt.getChild(i, Cond.class);
                    }
                    else if(stmt.getChild(i, ForStmt.class)!=null && flag == 2) {
                        forStmt2 = stmt.getChild(i, ForStmt.class);
                    }
                }
                BasicBlock condBlock = f.buildBasicBlock(curFunction);
                BasicBlock trueBlock = f.buildBasicBlock(curFunction);
                BasicBlock nextBlock = f.buildBasicBlock(curFunction);
                BasicBlock changeBlock = f.buildBasicBlock(curFunction);
                forBB = changeBlock;
                forNextBB = nextBlock;
                if(forStmt1 != null)  visitForStmt(forStmt1);
                f.buildBrInst(condBlock, curBB);
                curBB = condBlock;
                if(cond == null) {
                    IntConst intConst = new IntConst(new Word("1", TokenType.INTCON, 0));
                    Number number = new Number();
                    PrimaryExp primaryExp = new PrimaryExp();
                    UnaryExp unaryExp = new UnaryExp();
                    MulExp mulExp = new MulExp();
                    AddExp addExp = new AddExp();
                    RelExp relExp = new RelExp();
                    EqExp eqExp = new EqExp();
                    LAndExp lAndExp = new LAndExp();
                    LOrExp lOrExp = new LOrExp();
                    cond = new Cond();
                    number.addChild(intConst);
                    primaryExp.addChild(number);
                    unaryExp.addChild(primaryExp);
                    mulExp.addChild(unaryExp);
                    addExp.addChild(mulExp);
                    relExp.addChild(addExp);
                    eqExp.addChild(relExp);
                    lAndExp.addChild(eqExp);
                    lOrExp.addChild(lAndExp);
                    cond.addChild(lOrExp);
                }
                visitCond(cond, trueBlock, nextBlock);
                curBB = trueBlock;
                visitStmt(stmt.getChild(stmt.getChild().size() - 1, Stmt.class));
                f.buildBrInst(changeBlock, curBB);
                curBB = changeBlock;
                if(forStmt2 != null)  visitForStmt(forStmt2);
                f.buildBrInst(condBlock, curBB);
                curBB = nextBlock;
            } else if (str.equals("break")) {
                f.buildBrInst(forNextBB, curBB);
            } else if (str.equals("continue")) {
                f.buildBrInst(forBB, curBB);
            } else if (str.equals("return")) {
                if (stmt.getChild(1, Exp.class) != null) {
                    visitExp(stmt.getChild(1, Exp.class), false);
                    f.buildRetInst(curValue, curBB);
                } else f.buildRetInst(curBB);
            } else if (str.equals("printf")) {
                String formatStr = stmt.getChild(2);
                int cnt = 2;
                for (int i = 1; i < formatStr.length() - 1; i++) {
                    if (formatStr.charAt(i) == '%') {
                        if (formatStr.charAt(i + 1) == 'd') {
                            visitExp(stmt.getChild(2 + cnt, Exp.class), false);
                            cnt += 2;
                            f.buildCallInst(func_putint, new ArrayList<>() {{
                                add(curValue);
                            }}, curBB);
                            i++;
                        }
                    } else if (formatStr.charAt(i) == '\\') {
                        if (formatStr.charAt(i + 1) == 'n') {
                            f.buildCallInst(func_putch, new ArrayList<>() {{
                                add(f.buildNumber("10"));
                            }}, curBB);
                            i++;
                        }
                    } else {
                        int finalI = i;
                        f.buildCallInst(func_putch, new ArrayList<>() {{
                            add(new Value(String.valueOf((int) formatStr.charAt(finalI)), IntType.I32));
                        }}, curBB);
                    }
                }
            }
        }
    }

    public void visitForStmt(ForStmt forStmt) {
        visitLVal(forStmt.getChild(0, LVal.class), true);
        Value tmpValue = curValue;
        visitExp(forStmt.getChild(2, Exp.class), false);
        f.buildStoreInst(curValue, tmpValue, curBB);
    }

    public void visitConstExp(ConstExp constExp, boolean isGlobal) {
        if (isGlobal) {
            GlobalInt = visitGlobalAddExp(constExp.getChild(0, AddExp.class));
//            getExpValue(constExp.getChild(0, AddExp.class));
        } else
            visitAddExp(constExp.getChild(0, AddExp.class));
    }

    public void visitExp(Exp exp, boolean isGlobal) {
        if (isGlobal)
            GlobalInt = visitGlobalAddExp(exp.getChild(0, AddExp.class));
        else
            visitAddExp(exp.getChild(0, AddExp.class));
    }

    public void visitCond(Cond cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        // Cond → LOrExp
        visitLorExp(cond.getChild(0, LOrExp.class), trueBlock, falseBlock);
    }

    public void visitLVal(LVal lVal, boolean isAssign) {
        // LVal → Ident {'[' Exp ']'}
        Ident ident = lVal.getChild(0, Ident.class);
        curValue = querySymbol(ident.getWord().getToken());
        assert curValue != null;
        if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
        //Array   Const?


    }

    public void visitPrimaryExp(PrimaryExp primaryExp) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if (primaryExp.getChild(0, Terminal.class) != null) {
            visitExp(primaryExp.getChild(1, Exp.class), false);
        } else if (primaryExp.getChild(0, LVal.class) != null) {
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
        if (node instanceof UnaryOp) {
            String str = unaryExp.getChild(0, UnaryOp.class).getChild(0);
            switch (str) {
                case "+" -> visitUnaryExp(unaryExp.getChild(1, UnaryExp.class));
                case "-" -> {
                    visitUnaryExp(unaryExp.getChild(1, UnaryExp.class));
                    curValue = f.buildBinaryInst(f.buildNumber("0"), curValue, Operator.Sub, curBB);
                }
                case "!" -> {
                    visitUnaryExp(unaryExp.getChild(1, UnaryExp.class));
                    curValue = f.buildBinaryInst(curValue, f.buildNumber("0"), Operator.Eq, curBB);
                }
            }
        } else if (node instanceof Ident) {
            String ident = unaryExp.getChild(0);
            Function function = (Function) querySymbol(ident);
            ArrayList<Value> values = new ArrayList<>();
            if (unaryExp.getChild(2, FuncRParams.class) != null) {
                FuncRParams funcRParams = unaryExp.getChild(2, FuncRParams.class);
                for (int i = 0; i < funcRParams.getChild().size(); i += 2) {
                    visitExp(funcRParams.getChild(i, Exp.class), false);
                    values.add(curValue);
                }
            }
            curValue = f.buildCallInst(function, values, curBB);
        } else
            visitPrimaryExp(unaryExp.getChild(0, PrimaryExp.class));
    }

    public void visitMulExp(MulExp mulExp) {
        ASTNode node = mulExp.getChild().get(0);
        if (node instanceof UnaryExp)
            visitUnaryExp(mulExp.getChild(0, UnaryExp.class));
        else {
            visitMulExp(mulExp.getChild(0, MulExp.class));
            for (int i = 2; i < mulExp.getChild().size() - 2; i += 2) {
                Value tmpValue = curValue;
                visitMulExp(mulExp.getChild(i, MulExp.class));
                Operator op = Operator.str2op(mulExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitUnaryExp(mulExp.getChild(mulExp.getChild().size() - 1, UnaryExp.class));
            Operator op = Operator.str2op(mulExp.getChild(mulExp.getChild().size() - 2));
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void visitAddExp(AddExp addExp) {
        ASTNode node = addExp.getChild().get(0);
        if (node instanceof MulExp)
            visitMulExp(addExp.getChild(0, MulExp.class));
        else {
            visitAddExp(addExp.getChild(0, AddExp.class));
            for (int i = 2; i < addExp.getChild().size() - 2; i += 2) {
                Value tmpValue = curValue;
                visitAddExp(addExp.getChild(i, AddExp.class));
                Operator op = Operator.str2op(addExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitMulExp(addExp.getChild(addExp.getChild().size() - 1, MulExp.class));
            Operator op = Operator.str2op(addExp.getChild(addExp.getChild().size() - 2));
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void visitRelExp(RelExp relExp) {
        ASTNode node = relExp.getChild().get(0);
        if (node instanceof AddExp)
            visitAddExp(relExp.getChild(0, AddExp.class));
        else {
            visitRelExp(relExp.getChild(0, RelExp.class));
            for (int i = 2; i < relExp.getChild().size() - 2; i += 2) {
                Value tmpValue = curValue;
                visitRelExp(relExp.getChild(i, RelExp.class));
                Operator op = Operator.str2op(relExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitAddExp(relExp.getChild(relExp.getChild().size() - 1, AddExp.class));
            Operator op = Operator.str2op(relExp.getChild(relExp.getChild().size() - 2));
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void visitEqExp(EqExp eqExp) {
        ASTNode node = eqExp.getChild().get(0);
        if (node instanceof RelExp)
            visitRelExp(eqExp.getChild(0, RelExp.class));
        else {
            visitEqExp(eqExp.getChild(0, EqExp.class));
            for (int i = 2; i < eqExp.getChild().size() - 2; i += 2) {
                Value tmpValue = curValue;
                visitEqExp(eqExp.getChild(i, EqExp.class));
                Operator op = Operator.str2op(eqExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitRelExp(eqExp.getChild(eqExp.getChild().size() - 1, RelExp.class));
            Operator op = Operator.str2op(eqExp.getChild(eqExp.getChild().size() - 2));
            curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
        }
    }

    public void visitLAndExp(LAndExp lAndExp, BasicBlock trueBlock, BasicBlock falseBlock) {
        ASTNode node = lAndExp.getChild().get(0);
        if (node instanceof EqExp)
            visitEqExp(lAndExp.getChild(0, EqExp.class));
        else {
            for (int i = 0; i < lAndExp.getChild().size() - 2; i += 2) {
                BasicBlock nextBlock = f.buildBasicBlock(curFunction);
                visitLAndExp(lAndExp.getChild(i, LAndExp.class), nextBlock, falseBlock);
                curBB = nextBlock;
            }
            visitEqExp(lAndExp.getChild(lAndExp.getChild().size() - 1, EqExp.class));
        }
        curValue = f.buildBinaryInst(curValue, f.buildNumber("0"), Operator.Ne, curBB);
        f.buildBrInst(curValue, trueBlock, falseBlock, curBB);
    }

    public void visitLorExp(LOrExp lOrExp, BasicBlock trueBlock, BasicBlock falseBlock) {
        ASTNode node = lOrExp.getChild().get(0);
        if (node instanceof LAndExp)
            visitLAndExp(lOrExp.getChild(0, LAndExp.class), trueBlock, falseBlock);
        else {
            for (int i = 0; i < lOrExp.getChild().size() - 2; i += 2) {
                BasicBlock nextBlock = f.buildBasicBlock(curFunction);
                visitLorExp(lOrExp.getChild(i, LOrExp.class), trueBlock, nextBlock);
                curBB = nextBlock;
            }
            visitLAndExp(lOrExp.getChild(lOrExp.getChild().size() - 1, LAndExp.class), trueBlock, falseBlock);
        }
//        curValue = f.buildBinaryInst(curValue, f.buildNumber("0"), Operator.Ne, curBB);
//        f.buildBrInst(curValue, trueBlock, falseBlock, curBB);
    }

    public void initLibFunc() {
        func_putint.addArg(new Argument("x", IntType.I32, func_putint));
        func_putch.addArg(new Argument("c", IntType.I32, func_putch));
        func_putstr.addArg(new Argument("str", new PointerType(IntType.I8), func_putstr));
    }

    public int visitGlobalAddExp(AddExp addExp) {
        ASTNode node = addExp.getChild().get(0);
        if (node instanceof MulExp)
            return visitGlobalMulExp(addExp.getChild(0, MulExp.class));
        else {
            int x, y;
            x = visitGlobalAddExp(addExp.getChild(0, AddExp.class));
            for (int i = 2; i < addExp.getChild().size() - 2; i += 2) {
                y = visitGlobalAddExp(addExp.getChild(i, AddExp.class));
                Operator op = addExp.getChild(i - 1).equals("+") ? Operator.Add : Operator.Sub;
                x = cal(x, y, op);
            }
            y = visitGlobalMulExp(addExp.getChild(addExp.getChild().size() - 1, MulExp.class));
            Operator op = addExp.getChild(addExp.getChild().size() - 2).equals("+") ? Operator.Add : Operator.Sub;
            x = cal(x, y, op);
            return x;
        }
    }

    public int visitGlobalMulExp(MulExp mulExp) {
        ASTNode node = mulExp.getChild().get(0);
        if (node instanceof UnaryExp)
            return visitGlobalUnaryExp(mulExp.getChild(0, UnaryExp.class));
        else {
            int x, y;
            x = visitGlobalMulExp(mulExp.getChild(0, MulExp.class));
            for (int i = 2; i < mulExp.getChild().size() - 2; i += 2) {
                y = visitGlobalMulExp(mulExp.getChild(i, MulExp.class));
                Operator op = switch (mulExp.getChild(i - 1)) {
                    case "*" -> Operator.Mul;
                    case "/" -> Operator.Div;
                    case "%" -> Operator.Mod;
                    default -> throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(i - 1));
                };
                x = cal(x, y, op);
            }
            y = visitGlobalUnaryExp(mulExp.getChild(mulExp.getChild().size() - 1, UnaryExp.class));
            Operator op = switch (mulExp.getChild(mulExp.getChild().size() - 2)) {
                case "*" -> Operator.Mul;
                case "/" -> Operator.Div;
                case "%" -> Operator.Mod;
                default ->
                        throw new IllegalStateException("Unexpected value in MulExp: " + mulExp.getChild(mulExp.getChild().size() - 2));
            };
            x = cal(x, y, op);
            return x;
        }
    }

    public int visitGlobalUnaryExp(UnaryExp unaryExp) {
        ASTNode node = unaryExp.getChild().get(0);
        if (node instanceof UnaryOp) {
            String str = unaryExp.getChild(0, UnaryOp.class).getChild(0);
            if (str.equals("+")) {
                return visitGlobalUnaryExp(unaryExp.getChild(1, UnaryExp.class));
            } else {
                return -visitGlobalUnaryExp(unaryExp.getChild(1, UnaryExp.class));
            }
        } else
            return visitGlobalPrimaryExp(unaryExp.getChild(0, PrimaryExp.class));
    }

    public int visitGlobalPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.getChild(0, Terminal.class) != null) {
            return visitGlobalAddExp(primaryExp.getChild(1, Exp.class).getChild(0, AddExp.class));
        } else if (primaryExp.getChild(0, LVal.class) != null) {
            String name = primaryExp.getChild(0, LVal.class).getChild(0);
            for (GlobalVar globalVar : globalVars) {
                if (globalVar.getName().equals("@" + name))
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
