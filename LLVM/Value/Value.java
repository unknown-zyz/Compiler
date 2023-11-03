package LLVM.Value;

import LLVM.Use;
import LLVM.type.Type;

import java.util.ArrayList;

public class Value {
    private String name;
    private Type type;
    private ArrayList<Use> useList;

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
}
