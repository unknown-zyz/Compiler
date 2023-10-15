package Syntax.Node;

import Error.*;
import Symbols.SymbolTable;

import static Syntax.SyntaxMain.*;
import static Syntax.Node.FuncDef.isFuncReturn;


public class Stmt extends non_Terminal {
    private static int cnt;    //FormatString中%出现次数
    public static void addCnt() {
        cnt++;
    }
    @Override
    public void analyse() {
        boolean isStmtReturn = false;
        // Block
        if(cur_equal("{")) {
            //新作用域
            addSymbolTable(new SymbolTable(curTable));
            add_analyse(new Block());
        }
        // ;
        else if(cur_equal(";")) {
            addChild(new Symbol(cur));
            next();
        }
        // Exp;//注意此处没有isIdent，留到后面判断
        else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")|| isIntConst(cur)) {
            add_analyse(new Exp());
            if(cur_equal(";")) {
                addChild(new Symbol(cur));
                next();
            }
            else
                addError(ErrorType.i);
        }
        else if(cur_equal("if")) {
            addChild(new Reserved(cur));
            next();
            if(cur_equal("(")) {
                addChild(new Symbol(cur));
                next();
                add_analyse(new Cond());
                if(cur_equal(")")) {
                    addChild(new Symbol(cur));
                    next();
                    add_analyse(new Stmt());
                    if(cur_equal("else")) {
                        addChild(new Reserved(cur));
                        next();
                        add_analyse(new Stmt());
                    }
                }
                else
                    addError(ErrorType.j);
            }
        }
        else if(cur_equal("for"))
        {
            forFlag++;
            addChild(new Reserved(cur));
            next();
            if(cur_equal("("))
            {
                addChild(new Symbol(cur));
                next();
                if(isIdent(cur))
                    add_analyse(new ForStmt());
                if(cur_equal(";"))
                {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
                        add_analyse(new Cond());
                    if(cur_equal(";"))
                    {
                        addChild(new Symbol(cur));
                        next();
                        if(isIdent(cur))
                            add_analyse(new ForStmt());
                        if(cur_equal(")"))
                        {
                            addChild(new Symbol(cur));
                            next();
                            add_analyse(new Stmt());
                        }
                    }
                    else
                        addError(ErrorType.i);
                }
                else
                    addError(ErrorType.i);
            }
            else
                addError(ErrorType.j);
            forFlag--;
        }
        else if(cur_equal("continue")||cur_equal("break")) {
            if(forFlag==0)
                addError(ErrorType.m, cur.getLine());
            addChild(new Reserved(cur));
            next();
            if(cur_equal(";"))
            {
                addChild(new Symbol(cur));
                next();
            }
            else
                addError(ErrorType.i);
        }
        else if(cur_equal("return"))
        {
            addChild(new Reserved(cur));
            next();
            if(cur_equal(";")){
                addChild(new Symbol(cur));
                next();
            }
            else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
            {
                isStmtReturn = true;
                isFuncReturn = true;
                add_analyse(new Exp());
                if(cur_equal(";"))
                {
                    addChild(new Symbol(cur));
                    next();
                }
                else
                    addError(ErrorType.i);
            }
            else
                addError(ErrorType.i);
        }
        else if(cur_equal("printf")) {
            cnt = 0;
            boolean flag = true;    //字符串是否合法
            addChild(new Reserved(cur));
            next();
            if(cur_equal("(")) {
                addChild(new Reserved(cur));
                next();
                if(!isFormatString(cur))
                {
                    flag = false;
                    addError(ErrorType.a);
                }

                addChild(new FormatString(cur));
                next();
                int expCnt = 0;
                while(cur_equal(","))
                {
                    expCnt++;
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
                        add_analyse(new Exp());
                }
                if(cnt != expCnt && flag)
                    addError(ErrorType.l);
                if(cur_equal(")")) {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal(";")) {
                        addChild(new Symbol(cur));
                        next();
                    }
                    else
                        addError(ErrorType.i);
                }
                else
                    addError(ErrorType.j);
            }
        }
        else if(isIdent(cur)) {
            if(isLVal())
            {
                if(isConstSymbol(cur.getToken()))
                    addError(ErrorType.h, cur.getLine());
                add_analyse(new LVal());
                if(cur_equal("=")) {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("getint")) {
                        addChild(new Reserved(cur));
                        next();
                        if(cur_equal("("))
                        {
                            addChild(new Symbol(cur));
                            next();
                            if(cur_equal(")"))
                            {
                                addChild(new Symbol(cur));
                                next();
                                if(cur_equal(";"))
                                {
                                    addChild(new Symbol(cur));
                                    next();
                                }
                                else
                                    addError(ErrorType.i);
                            }
                            else
                                addError(ErrorType.j);
                        }
                    }
                    else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur)) {
                        add_analyse(new Exp());
                        if(cur_equal(";")) {
                            addChild(new Symbol(cur));
                            next();
                        }
                        else
                            addError(ErrorType.i);
                    }
                }
            }
            else {
                add_analyse(new Exp());
                if(cur_equal(";")) {
                    addChild(new Symbol(cur));
                    next();
                }
                else
                    addError(ErrorType.i);
            }
        }
        if(!needReturn && isStmtReturn)
            addError(ErrorType.f);
    }
}
