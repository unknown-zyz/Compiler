package Midend.LLVM.Value;

import Midend.LLVM.Use;
import Midend.LLVM.Type.Type;

import java.util.ArrayList;

public class Value {
    private String name;
    private final Type type;
    private final ArrayList<Use> useList;

    public static int valCnt = -1;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        useList = new ArrayList<>();
    }

    public void addUse(Use use){
        useList.add(use);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString(){
        return this.type + " " + this.name;
    }

    public void removeOneUse(User user){
        int index = -1;
        for(int i = 0; i < useList.size(); i++){
            if(useList.get(i).getUser().equals(user)){
                index = i;
                break;
            }
        }
        useList.remove(index);
    }

    public void removeAllUse() {
        useList.clear();
    }

    public void replace(Value value) {
        for (Use use : useList) {
            User user = use.getUser();
            int index = user.getOperands().indexOf(this);
            user.setOperand(index, value);
        }
        removeAllUse();
    }
}
