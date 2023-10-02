package Syntax.Node;

import static Syntax.SyntaxMain.*;

public class FuncFParam extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("int")) {
            addChild(new Reserved(cur));
            next();
            if(isIdent(cur))
            {
                addChild(new Ident(cur));
                next();
                if(cur_equal("["))
                {
                    addChild(new Symbol(cur));
                    next();
                    if(cur_equal("]"))
                    {
                        addChild(new Symbol(cur));
                        next();
                    }
//                    else
//                        System.out.println("error k"+getBefore().getLine());
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
//                        else
//                            System.out.println("error k"+getBefore().getLine());
                    }
                }
            }
        }
    }
}
