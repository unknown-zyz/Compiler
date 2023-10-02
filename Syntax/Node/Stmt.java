package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class Stmt extends non_Terminal {
    @Override
    public void analyse() {
        // Block
        if(cur_equal("{")) {
            add_analyse(new Block());
        }
        // ;
        else if(cur_equal(";")) {
            addChild(new Symbol(cur));
            next();
        }
        // Exp;
        else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")|| isIntConst(cur)) {
            add_analyse(new Exp());
            if(cur_equal(";")) {
                addChild(new Symbol(cur));
                next();
            }
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
                else {}
            }
            else {}
        }
        else if(cur_equal("for")) {
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
                }
            }
        }
        else if(cur_equal("continue")||cur_equal("break")) {
            addChild(new Reserved(cur));
            next();
            if(cur_equal(";"))
            {
                addChild(new Symbol(cur));
                next();
            }
        }
        else if(cur_equal("return")) {
            addChild(new Reserved(cur));
            next();
            if(cur_equal(";")){
                addChild(new Symbol(cur));
                next();
            }
            else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
            {
                add_analyse(new Exp());
                if(cur_equal(";"))
                {
                    addChild(new Symbol(cur));
                    next();
                }
            }
        }
        else if(cur_equal("printf")) {
            addChild(new Reserved(cur));
            next();
            if(cur_equal("(")) {
                addChild(new Reserved(cur));
                next();
                if(isFormatString(cur)) {
                    addChild(new FormatString(cur));
                    next();
                    while(cur_equal(","))
                    {
                        addChild(new Symbol(cur));
                        next();
                        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
                            add_analyse(new Exp());
                    }
                    if(cur_equal(")")) {
                        addChild(new Symbol(cur));
                        next();
                        if(cur_equal(";")) {
                            addChild(new Symbol(cur));
                            next();
                        }
                    }
                }
            }
        }
        else if(isIdent(cur)) {
            if(isLVal())
            {
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
                            }
                        }
                    }
                    else if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur)) {
                        add_analyse(new Exp());
                        if(cur_equal(";")) {
                            addChild(new Symbol(cur));
                            next();
                        }
                    }
                    else {}
                }
                else {}
            }
            else {
                add_analyse(new Exp());
                if(cur_equal(";")) {
                    addChild(new Symbol(cur));
                    next();
                }
                else {}
            }
        }
        else {}
    }
}
