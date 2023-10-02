package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class LOrExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new LAndExp());
            while(cur_equal("||"))
            {
                LAndExp a = (LAndExp) this.removeChild();
                LOrExp lOrExp = new LOrExp();
                lOrExp.addChild(a);
                addChild(lOrExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new LAndExp());
            }
        }
    }
}
