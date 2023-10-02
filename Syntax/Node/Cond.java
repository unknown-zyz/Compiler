package Syntax.Node;

public class Cond extends non_Terminal {
    @Override
    public void analyse() {
        add_analyse(new LOrExp());
    }
}
