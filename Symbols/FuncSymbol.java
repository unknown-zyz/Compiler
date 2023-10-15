package Symbols;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private Functype functype;
    private ArrayList<FuncParam> funcParams = new ArrayList<>();

    public FuncSymbol(String name, Functype functype) {
        super(name);
        this.functype = functype;
    }

    public Functype getFunctype() {
        return functype;
    }

    public void addFuncParam(FuncParam funcParam) {
        funcParams.add(funcParam);
    }

    public int getParamCnt()    {return funcParams.size();}

    public ArrayList<Integer> getFuncParams() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(FuncParam funcParam: funcParams)
            arrayList.add(funcParam.getDimension());
        return arrayList;
    }

    @Override
    public String toString() {
        return "FuncSymbol{" +
                "name=" + getName() +
                ", functype=" + functype +
                ", funcParams=" + funcParams +
                '}';
    }
}
