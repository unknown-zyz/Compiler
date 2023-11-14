package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class InitVal extends non_Terminal {

    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
            add_analyse(new Exp());
        else if(cur_equal("{"))
        {
            addChild(new Symbol(cur));
            next();
            if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur)||cur_equal("{"))
            {
               add_analyse(new InitVal());
               while(cur_equal(","))
               {
                   addChild(new Symbol(cur));
                   next();
                   add_analyse(new InitVal());
               }
            }
            if(cur_equal("}"))
            {
                addChild(new Symbol(cur));
                next();
            }
        }
    }
}
