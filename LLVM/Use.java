package LLVM;

import LLVM.Value.User;
import LLVM.Value.Value;

public class Use {
    private User user;
    private Value value;
    private int pos;

    public Use(User user, Value value) {
        this.user = user;
        this.value = value;
    }
}
