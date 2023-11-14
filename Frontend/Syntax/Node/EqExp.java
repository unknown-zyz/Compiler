package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class EqExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new RelExp());
            while (cur_equal("==")||cur_equal("!="))
            {
                RelExp a = (RelExp) this.removeChild();
                EqExp eqExp = new EqExp();
                eqExp.addChild(a);
                addChild(eqExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new RelExp());
            }
        }
    }
}
