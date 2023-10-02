package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class LAndExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new EqExp());
            while (cur_equal("&&"))
            {
                EqExp a = (EqExp) this.removeChild();
                LAndExp lAndExp = new LAndExp();
                lAndExp.addChild(a);
                addChild(lAndExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new EqExp());
            }
        }
    }
}
