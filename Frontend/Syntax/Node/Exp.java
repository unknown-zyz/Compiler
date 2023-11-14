package Frontend.Syntax.Node;

public class Exp extends non_Terminal {
    @Override
    public void analyse() {
        add_analyse(new AddExp());
    }
}
