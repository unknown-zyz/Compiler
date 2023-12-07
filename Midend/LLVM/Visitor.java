package Midend.LLVM;

import Midend.LLVM.Value.*;
import Midend.LLVM.Value.Instruction.AllocInst;
import Midend.LLVM.Value.Instruction.Operator;
import Midend.LLVM.Type.*;
import Frontend.Lexical.TokenType;
import Frontend.Lexical.Word;
import Frontend.Syntax.Node.*;
import Frontend.Syntax.Node.Number;

import java.util.ArrayList;
import java.util.HashMap;

public class Visitor {
    private IRModule module;
    private Function curFunction;
    private BasicBlock curBB;
    private Value curValue;
    private final ArrayList<HashMap<String, Value>> symbolTables = new ArrayList<>();
    private final IRFactory f = IRFactory.getInstance();
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

    public void printSymbolTable() {
        for (HashMap<String, Value> symbolTable : symbolTables) {
            symbolTable.forEach((key, value) -> {
                System.out.print(key + " " + value);
                System.out.println(" "+value.getVal());
            });
        }
    }

    //库函数
    private final Function func_getint = new Function("getint", IntType.I32);
    private final Function func_putint = new Function("putint", VoidType.voidType);
    private final Function func_putch = new Function("putch", VoidType.voidType);
    private final Function func_putstr = new Function("putstr", VoidType.voidType);


    public IRModule visit(CompUnit compUnit) {
        ArrayList<GlobalVar> globalVars = new ArrayList<>();
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
        //printSymbolTable();
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
            visitArray(constDef, type, isGlobal);
        } else {
            if (isGlobal) {
                visitConstInitVal(constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type), new Value(String.valueOf(GlobalInt), IntType.I32), true);
                module.addGlobalVar((GlobalVar) curValue);
            } else {
                curValue = f.buildAllocInst(new PointerType(type), curBB);
                if (constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class) != null) {
                    Value tmpValue = curValue;
                    visitConstInitVal(constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class), false);
                    f.buildStoreInst(curValue, tmpValue, curBB);
                    curValue = tmpValue;
                    //bug?
                    visitConstInitVal(constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class), true);
                    curValue.setVal(GlobalInt);
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
            visitArray(varDef, type, isGlobal);
        } else {
            if (isGlobal) {
                if (varDef.getChild(varDef.getChild().size() - 1, InitVal.class) != null)
                    visitInitVal(varDef.getChild(varDef.getChild().size() - 1, InitVal.class), true);
                curValue = f.buildGlobalVar(i_name, new PointerType(type), new Value(String.valueOf(GlobalInt), IntType.I32), false);
                module.addGlobalVar((GlobalVar) curValue);
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

    public void visitArray(ConstDef constDef, Type type, boolean isGlobal) {
        Ident ident = constDef.getChild(0, Ident.class);
        ConstInitVal constInitVal = constDef.getChild(constDef.getChild().size() - 1, ConstInitVal.class);
        String name = ident.getWord().getToken();
        int dim = ident.getDim();
        ArrayList<Integer> sizes = new ArrayList<>();
        ArrayList<Value> values = new ArrayList<>();
        sizes.add(visitGlobalAddExp(constDef.getChild(2, ConstExp.class).getChild(0, AddExp.class)));
        if (dim == 2)
            sizes.add(visitGlobalAddExp(constDef.getChild(5, ConstExp.class).getChild(0, AddExp.class)));
        if (dim == 1) {
            int cnt = 1, val = 0;
            for (int i = 0; i < sizes.get(0); i++) {
                if (cnt < constInitVal.getChild().size() - 1) {
                    if (isGlobal)
                        val = visitGlobalAddExp(constInitVal.getChild(cnt, ConstInitVal.class).getChild(0, ConstExp.class).getChild(0, AddExp.class));
                    else
                        visitConstExp(constInitVal.getChild(cnt, ConstInitVal.class).getChild(0, ConstExp.class), false);
                    cnt += 2;
                } else {
                    if (isGlobal)
                        val = 0;
                    else
                        curValue = new Value("0", IntType.I32);
                }
                if (isGlobal)
                    values.add(new Value(String.valueOf(val), IntType.I32));
                else
                    values.add(curValue);
            }
        } else {
            int cnt1 = 1, cnt2, val = 0;
            for (int i = 0; i < sizes.get(0); i++) {
                cnt2 = 1;
                for (int j = 0; j < sizes.get(1); j++) {
                    if (cnt1 < constInitVal.getChild().size() - 1 && cnt2 < constInitVal.getChild(cnt1, ConstInitVal.class).getChild().size() - 1) {
                        if (isGlobal)
                            val = visitGlobalAddExp(constInitVal.getChild(cnt1, ConstInitVal.class).getChild(cnt2, ConstInitVal.class).getChild(0, ConstExp.class).getChild(0, AddExp.class));
                        else
                            visitConstExp(constInitVal.getChild(cnt1, ConstInitVal.class).getChild(cnt2, ConstInitVal.class).getChild(0, ConstExp.class), false);
                        cnt2 += 2;
                    } else {
                        if (isGlobal)
                            val = 0;
                        else
                            curValue = new Value("0", IntType.I32);
                    }
                    if (isGlobal)
                        values.add(new Value(String.valueOf(val), IntType.I32));
                    else
                        values.add(curValue);
                }
                cnt1 += 2;
            }
        }
        if (isGlobal) {
            curValue = f.buildGlobalArray(name, type, sizes, values);
            module.addGlobalVar((GlobalVar) curValue);
        } else {
            ArrayType arrayType = f.buildArrayType(sizes, type);
            curValue = f.buildAllocInst(new PointerType(arrayType), curBB);
            Value tmpValue = curValue;
            ArrayList<Value> indexs = new ArrayList<>();
            for (int i = 0; i < dim; i++)
                indexs.add(new Value("0", IntType.I32));
            for (int i = 0; i < values.size(); i++) {
                indexs.add(new Value(String.valueOf(i), IntType.I32));
                curValue = f.buildGetPtrInst(tmpValue, indexs, curBB);
                f.buildStoreInst(values.get(i), curValue, curBB);
                indexs.remove(indexs.size() - 1);
            }
            curValue = tmpValue;
        }
        pushSymbol(name, curValue);
    }

    public void visitArray(VarDef varDef, Type type, boolean isGlobal) {
        Ident ident = varDef.getChild(0, Ident.class);
        InitVal initVal = varDef.getChild(varDef.getChild().size() - 1, InitVal.class);
        String name = ident.getWord().getToken();
        int dim = ident.getDim();
        ArrayList<Integer> sizes = new ArrayList<>();
        ArrayList<Value> values = new ArrayList<>();
        sizes.add(visitGlobalAddExp(varDef.getChild(2, ConstExp.class).getChild(0, AddExp.class)));
        if (dim == 2)
            sizes.add(visitGlobalAddExp(varDef.getChild(5, ConstExp.class).getChild(0, AddExp.class)));
        if (initVal != null) {
            if (dim == 1) {
                int cnt = 1, val = 0;
                for (int i = 0; i < sizes.get(0); i++) {
                    if (cnt < initVal.getChild().size() - 1) {
                        if (isGlobal)
                            val = visitGlobalAddExp(initVal.getChild(cnt, InitVal.class).getChild(0, Exp.class).getChild(0, AddExp.class));
                        else
                            visitExp(initVal.getChild(cnt, InitVal.class).getChild(0, Exp.class), false);
                        cnt += 2;
                    } else {
                        if (isGlobal)
                            val = 0;
                        else
                            curValue = new Value("0", IntType.I32);
                    }
                    if (isGlobal)
                        values.add(new Value(String.valueOf(val), IntType.I32));
                    else
                        values.add(curValue);
                }
            } else {
                int cnt1 = 1, cnt2, val = 0;
                for (int i = 0; i < sizes.get(0); i++) {
                    cnt2 = 1;
                    for (int j = 0; j < sizes.get(1); j++) {
                        if (cnt1 < initVal.getChild().size() - 1 && cnt2 < initVal.getChild(cnt1, InitVal.class).getChild().size() - 1) {
                            if (isGlobal)
                                val = visitGlobalAddExp(initVal.getChild(cnt1, InitVal.class).getChild(cnt2, InitVal.class).getChild(0, Exp.class).getChild(0, AddExp.class));
                            else
                                visitExp(initVal.getChild(cnt1, InitVal.class).getChild(cnt2, InitVal.class).getChild(0, Exp.class), false);
                            cnt2 += 2;
                        } else {
                            if (isGlobal)
                                val = 0;
                            else
                                curValue = new Value("0", IntType.I32);
                        }
                        if (isGlobal)
                            values.add(new Value(String.valueOf(val), IntType.I32));
                        else
                            values.add(curValue);
                    }
                    cnt1 += 2;
                }
            }
        } else {
            if (dim == 1) {
                for (int i = 0; i < sizes.get(0); i++)
                    values.add(new Value(String.valueOf(0), type));
            } else {
                for (int i = 0; i < sizes.get(0) * sizes.get(1); i++)
                    values.add(new Value(String.valueOf(0), type));
            }
        }
        if (isGlobal) {
            curValue = f.buildGlobalArray(name, type, sizes, values);
            module.addGlobalVar((GlobalVar) curValue);
        } else {
            ArrayType arrayType = f.buildArrayType(sizes, type);
            curValue = f.buildAllocInst(new PointerType(arrayType), curBB);
            if (initVal != null) {
                Value tmpValue = curValue;
                ArrayList<Value> indexs = new ArrayList<>();
                for (int i = 0; i < dim; i++)
                    indexs.add(new Value("0", IntType.I32));
                for (int i = 0; i < values.size(); i++) {
                    indexs.add(new Value(String.valueOf(i), IntType.I32));
                    curValue = f.buildGetPtrInst(tmpValue, indexs, curBB);
                    f.buildStoreInst(values.get(i), curValue, curBB);
                    indexs.remove(indexs.size() - 1);
                }
                curValue = tmpValue;
            }
        }
        pushSymbol(name, curValue);
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
        Argument argument;
        if (funcFParam.getChild().size() == 2) {
            argument = f.buildArgument(name, type, curFunction);
        } else if (funcFParam.getChild().size() == 4) {
            argument = f.buildArgument(name, type + "*", curFunction);
        } else {
            ArrayList<Integer> indexs = new ArrayList<>();
            for (int i = 5; i < funcFParam.getChild().size(); i += 3)
                indexs.add(visitGlobalAddExp(funcFParam.getChild(i, ConstExp.class).getChild(0, AddExp.class)));
            argument = f.buildArgument(name, type, curFunction, indexs);
        }
        AllocInst allocInst = f.buildAllocInst(new PointerType(argument.getType()), curBB);
        f.buildStoreInst(argument, allocInst, curBB);
        pushSymbol(name, allocInst);
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
            switch (str) {
                case "if" -> {
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
                }
                case "for" -> {
                    ForStmt forStmt1 = null, forStmt2 = null;
                    Cond cond = null;
                    int flag = 0;   //记录;数量来判断位置

                    for (int i = 2; i < stmt.getChild().size() - 2; i++) {
                        if (stmt.getChild(i) != null && stmt.getChild(i).equals(";")) {
                            flag++;
                        } else if (stmt.getChild(i, ForStmt.class) != null && flag == 0) {
                            forStmt1 = stmt.getChild(i, ForStmt.class);
                        } else if (stmt.getChild(i, Cond.class) != null && flag == 1) {
                            cond = stmt.getChild(i, Cond.class);
                        } else if (stmt.getChild(i, ForStmt.class) != null && flag == 2) {
                            forStmt2 = stmt.getChild(i, ForStmt.class);
                        }
                    }
                    BasicBlock condBlock = f.buildBasicBlock(curFunction);
                    BasicBlock trueBlock = f.buildBasicBlock(curFunction);
                    BasicBlock nextBlock = f.buildBasicBlock(curFunction);
                    BasicBlock changeBlock = f.buildBasicBlock(curFunction);
                    forBB = changeBlock;
                    forNextBB = nextBlock;
                    if (forStmt1 != null) visitForStmt(forStmt1);
                    f.buildBrInst(condBlock, curBB);
                    curBB = condBlock;
                    if (cond == null) {
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
                    if (forStmt2 != null) visitForStmt(forStmt2);
                    f.buildBrInst(condBlock, curBB);
                    curBB = nextBlock;
                }
                case "break" -> f.buildBrInst(forNextBB, curBB);
                case "continue" -> f.buildBrInst(forBB, curBB);
                case "return" -> {
                    if (stmt.getChild(1, Exp.class) != null) {
                        visitExp(stmt.getChild(1, Exp.class), false);
                        f.buildRetInst(curValue, curBB);
                    } else f.buildRetInst(curBB);
                }
                case "printf" -> {
                    StringBuilder sb = new StringBuilder();
                    String formatStr = stmt.getChild(2);
                    int cnt = 2;
                    for (int i = 1; i < formatStr.length() - 1; i++) {
                        if (formatStr.charAt(i) == '%') {
                            if (formatStr.charAt(i + 1) == 'd') {
                                module.getStringList().add(sb.toString());
                                sb.delete(0, sb.length());
                                visitExp(stmt.getChild(2 + cnt, Exp.class), false);
                                cnt += 2;
                                f.buildCallInst(func_putint, new ArrayList<>() {{
                                    add(curValue);
                                }}, curBB);
                                i++;
                            }
                            else {
                                sb.append(formatStr.charAt(i));
                            }
                        } else if (formatStr.charAt(i) == '\\') {
                            sb.append(formatStr.charAt(i));
                            if (formatStr.charAt(i + 1) == 'n') {
                                sb.append(formatStr.charAt(i+1));
                                f.buildCallInst(func_putch, new ArrayList<>() {{
                                    add(f.buildNumber("10"));
                                }}, curBB);
                                i++;
                            }
                        } else {
                            sb.append(formatStr.charAt(i));
                            int finalI = i;
                            f.buildCallInst(func_putch, new ArrayList<>() {{
                                add(new Value(String.valueOf((int) formatStr.charAt(finalI)), IntType.I32));
                            }}, curBB);
                        }
                    }
                    module.getStringList().add(sb.toString());
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
        }
        else if(canExpressionBeSimplified(constExp))
            curValue = f.buildNumber(String.valueOf(visitGlobalAddExp(constExp.getChild(0, AddExp.class))));
        else
            visitAddExp(constExp.getChild(0, AddExp.class));
    }

    public void visitExp(Exp exp, boolean isGlobal) {
        if (isGlobal)
            GlobalInt = visitGlobalAddExp(exp.getChild(0, AddExp.class));
        else if(canExpressionBeSimplified(exp))
            curValue = f.buildNumber(String.valueOf(visitGlobalAddExp(exp.getChild(0, AddExp.class))));
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
        int dim = 0;
        Value exp1 = null, exp2 = null;
        if (lVal.getChild().size() == 4) {
            dim = 1;
//            exp1 = visitGlobalAddExp(lVal.getChild(2, Exp.class).getChild(0, AddExp.class));
            visitExp(lVal.getChild(2, Exp.class), false);
            exp1 = curValue;
        } else if (lVal.getChild().size() == 7) {
            dim = 2;
            visitExp(lVal.getChild(2, Exp.class), false);
            exp1 = curValue;
            visitExp(lVal.getChild(5, Exp.class), false);
            exp2 = curValue;
        }
        curValue = querySymbol(ident.getWord().getToken());
        assert curValue != null;

        Type type = curValue.getType();
        assert type instanceof PointerType;
        ArrayList<Value> indexs = new ArrayList<>();
        if (((PointerType) type).getpType() instanceof ArrayType arrayType) {
            if (arrayType.getElementType() instanceof ArrayType) //二维
            {
                if (dim == 2) {
                    indexs.add(new Value("0", IntType.I32));
                    indexs.add(exp1);
                    indexs.add(exp2);
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                    if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
                } else if (dim == 1) {
                    indexs.add(new Value("0", IntType.I32));
                    indexs.add(exp1);
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                } else {
                    indexs.add(new Value("0", IntType.I32));
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                }
            } else    //一维
            {
                if (dim == 1) {
                    indexs.add(new Value("0", IntType.I32));
                    indexs.add(exp1);
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                    if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
                } else {
                    indexs.add(new Value("0", IntType.I32));
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                }
            }
        } else if (((PointerType) type).getpType() instanceof PointerType pointerType) {
            curValue = f.buildLoadInst(curValue, curBB);
            if (pointerType.getpType() instanceof ArrayType) //二维
            {
                if (dim == 2) {
                    indexs.add(exp1);
                    indexs.add(exp2);
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                    if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
                } else if (dim == 1) {
                    indexs.add(exp1);
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                } else {
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                }
            } else    //一维
            {
                if (dim == 1) {
                    indexs.add(exp1);
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                    if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
                } else {
                    indexs.add(new Value("0", IntType.I32));
                    curValue = f.buildGetPtrInst(curValue, indexs, curBB);
                }
            }
        } else {
            if (!isAssign) curValue = f.buildLoadInst(curValue, curBB);
        }
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
                if (tmpValue.getType() != curValue.getType()) {
                    if (tmpValue.getType() == IntType.I1)
                        tmpValue = f.buildBinaryInst(tmpValue, new Value("", IntType.I32), Operator.Zext, curBB);
                    else
                        curValue = f.buildBinaryInst(curValue, new Value("", IntType.I32), Operator.Zext, curBB);
                }
                Operator op = Operator.str2op(relExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitAddExp(relExp.getChild(relExp.getChild().size() - 1, AddExp.class));
            if (tmpValue.getType() != curValue.getType()) {
                if (tmpValue.getType() == IntType.I1)
                    tmpValue = f.buildBinaryInst(tmpValue, new Value("", IntType.I32), Operator.Zext, curBB);
                else
                    curValue = f.buildBinaryInst(curValue, new Value("", IntType.I32), Operator.Zext, curBB);
            }
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
                if (tmpValue.getType() != curValue.getType()) {
                    if (tmpValue.getType() == IntType.I1)
                        tmpValue = f.buildBinaryInst(tmpValue, new Value("", IntType.I32), Operator.Zext, curBB);
                    else
                        curValue = f.buildBinaryInst(curValue, new Value("", IntType.I32), Operator.Zext, curBB);
                }
                Operator op = Operator.str2op(eqExp.getChild(i - 1));
                curValue = f.buildBinaryInst(tmpValue, curValue, op, curBB);
            }
            Value tmpValue = curValue;
            visitRelExp(eqExp.getChild(eqExp.getChild().size() - 1, RelExp.class));
            if (tmpValue.getType() != curValue.getType()) {
                if (tmpValue.getType() == IntType.I1)
                    tmpValue = f.buildBinaryInst(tmpValue, new Value("", IntType.I32), Operator.Zext, curBB);
                else
                    curValue = f.buildBinaryInst(curValue, new Value("", IntType.I32), Operator.Zext, curBB);
            }
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
            LVal lVal = primaryExp.getChild(0, LVal.class);
            int len = lVal.getChild().size();
            String name = lVal.getChild(0);
            Value value = querySymbol(name);
            if(value instanceof GlobalVar globalVar) {
                if(len == 1)
                {
                    return Integer.parseInt(globalVar.getValue().getName());
                }
                else if(len == 4)
                {
                    int index1 = visitGlobalAddExp(lVal.getChild(2, Exp.class).getChild(0, AddExp.class));
                    return Integer.parseInt(globalVar.getValues().get(index1).getName());
                }
                else {
                    int index1 = visitGlobalAddExp(lVal.getChild(2, Exp.class).getChild(0, AddExp.class));
                    int index2 = visitGlobalAddExp(lVal.getChild(5, Exp.class).getChild(0, AddExp.class));
                    Type type = ((PointerType)globalVar.getType()).getpType();
                    int size = ((ArrayType)((ArrayType)type).getElementType()).getSize();
                    return Integer.parseInt(globalVar.getValues().get(index1*size + index2).getName());
                }
            } else if (value!=null)
            {
                return value.getVal();
            }
            System.out.println("Frontend.Semantic.Error: " + name + " not found!");
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

    public boolean canExpressionBeSimplified(non_Terminal nonTerminal) {
        for(ASTNode node: nonTerminal.getChild()) {
            if(node instanceof Terminal) {
                String token = ((Terminal)node).getWord().getToken();
                if(!(token.equals("+")||token.equals("-")||token.equals("*")||token.equals("/")||token.equals("%")||token.equals("(")||token.equals(")")||token.matches("\\d+")))
                    return false;
            }
            else {
                if(!canExpressionBeSimplified((non_Terminal)node))
                    return false;
            }
        }
        return true;
    }
}
