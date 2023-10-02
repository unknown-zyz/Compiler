package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class VarDef extends non_Terminal {
    @Override
    public void analyse() {
        if(isIdent(cur))
        {
            addChild(new Ident(cur));
            next();
            while(cur_equal("["))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new ConstExp());
                if(cur_equal("]"))
                {
                    addChild(new Symbol(cur));
                    next();
                }
//                else
//                    System.out.println("error k"+getBefore().getLine());
            }
            if(cur_equal("="))
            {
                addChild(new Symbol(cur));
                next();
                add_analyse(new InitVal());
            }
        }
    }
}
