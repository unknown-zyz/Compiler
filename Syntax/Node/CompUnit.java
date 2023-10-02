package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class CompUnit extends non_Terminal {
    @Override
    public void analyse() {
        while((cur_equal("const") && getNext().getToken().equals("int")) ||
                (cur_equal("int")&&isIdent(getNext())&&!getNextNext().getToken().equals("(")))
        {
            if(cur_equal("const"))
                add_analyse(new ConstDecl());
            else
                add_analyse(new VarDecl());
        }
        while((cur_equal("void") || cur_equal("int")) && isIdent(getNext()) && getNextNext().getToken().equals("("))
        {
            add_analyse(new FuncDef());
        }
        if(cur_equal("int") && getNext().getToken().equals("main"))
            add_analyse(new MainFuncDef());
    }
}
