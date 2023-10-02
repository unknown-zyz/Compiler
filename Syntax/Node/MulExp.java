package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class MulExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new UnaryExp());
            while (cur_equal("*")||cur_equal("/")||cur_equal("%"))
            {
                UnaryExp a = (UnaryExp) this.removeChild();
                MulExp mulExp = new MulExp();
                mulExp.addChild(a);
                addChild(mulExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new UnaryExp());
            }
        }
        else {}
    }
}
