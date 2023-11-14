package Midend.LLVM;

import Midend.LLVM.Value.User;
import Midend.LLVM.Value.Value;

public class Use {
    private User user;
    private Value value;
    private int pos;

    public Use(User user, Value value) {
        this.user = user;
        this.value = value;
    }
}
