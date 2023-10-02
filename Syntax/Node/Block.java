package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class Block extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("{")) {
            addChild(new Symbol(cur));
            next();
            while(cur_equal("const")||cur_equal("int")||isIdent(cur)||cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")|| isIntConst(cur)||cur_equal(";")|| cur_equal("{")||cur_equal("if")||cur_equal("for")||cur_equal("continue")||cur_equal("break")|| cur_equal("return")||cur_equal("printf"))
            {
                if(cur_equal("const")) {
                    add_analyse(new ConstDecl());
                }
                else if(cur_equal("int")) {
                    add_analyse(new VarDecl());
                }
                else if(isIdent(cur)||cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")|| isIntConst(cur)||cur_equal(";")||
                        cur_equal("{")||cur_equal("if")||cur_equal("for")||cur_equal("continue")||cur_equal("break")||
                        cur_equal("return")||cur_equal("printf"))
                {
                    add_analyse(new Stmt());
                }
                else {}
            }
            if(cur_equal("}")) {
                addChild(new Symbol(cur));
                next();
            }
            else {}
        }
    }
}
