package Syntax.Node;

import static Syntax.SyntaxMain.cur;
import static Syntax.SyntaxMain.next;

public class AddExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
        {
            add_analyse(new MulExp());
            while (cur_equal("+")||cur_equal("-"))
            {
                MulExp a = (MulExp) this.removeChild();
                AddExp addExp = new AddExp();
                addExp.addChild(a);
                addChild(addExp);
                addChild(new Symbol(cur));
                next();
                add_analyse(new MulExp());
            }
        }
    }
}
