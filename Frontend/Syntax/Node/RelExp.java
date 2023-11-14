package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.cur;
import static Frontend.Syntax.SyntaxMain.next;

public class RelExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new AddExp());
            while (cur_equal("<")||cur_equal(">")||cur_equal("<=")||cur_equal(">="))
            {
                AddExp a = (AddExp) this.removeChild();
                RelExp relExp = new RelExp();
                relExp.addChild(a);
                addChild(relExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new AddExp());
            }
        }
    }
}
