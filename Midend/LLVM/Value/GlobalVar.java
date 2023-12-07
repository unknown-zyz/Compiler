package Midend.LLVM.Value;

import Midend.LLVM.Type.ArrayType;
import Midend.LLVM.Type.PointerType;
import Midend.LLVM.Type.Type;

import java.util.ArrayList;

public class GlobalVar extends Value {
    private Value value;
    private ArrayList<Value> values;
    private final boolean isConst;
    private final boolean isArray;
    private int nowVisit;

    public GlobalVar(String name, Type type, Value value, boolean isConst) {
        super(name, type);
        this.value = value;
        this.isConst = isConst;
        this.isArray = false;
    }

    public GlobalVar(String name, Type type, ArrayList<Value> values) {
        super(name, type);
        this.values = values;
        this.isConst = false;
        this.isArray = true;
    }

    public Value getValue() {
        return value;
    }

    public ArrayList<Value> getValues() {
        return values;
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isZero() {
        if (!isArray) return false;
        if (values.size() == 0) return true;
        for (Value value : values) {
            if (!(value.getName().equals("0"))) return false;
        }
        return true;
    }

    public String initElementValue(ArrayType type) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" ");
        sb.append("[");
        int size = type.getSize();
        if (type.getElementType().isArrayType()) {
            for (int i = 0; i < size; i++) {
                sb.append(initElementValue((ArrayType) type.getElementType()));
                if (i != size - 1) sb.append(", ");
            }
        } else {
            for (int i = 0; i < size; i++) {
                sb.append(values.get(nowVisit++));
                if (i != size - 1) sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isArray) {
            sb.append(getName()).append(" = global ");
            Type ptype = ((PointerType) getType()).getpType();
            if (ptype instanceof ArrayType) {
                if (isZero()) sb.append(ptype).append(" zeroinitializer");
                else {
                    nowVisit = 0;
                    sb.append(initElementValue((ArrayType) ptype));
                }
            }
        } else {
            sb.append(getName()).append(" = ");
            if (isConst)
                sb.append("constant ");
            else
                sb.append("global ");
            sb.append(getValue().toString());
        }
        return sb.toString();
    }

    public String toMIPS() {
        StringBuilder sb = new StringBuilder();
        String name = getName().substring(1);
        sb.append(name);
        if (isArray) {
            ArrayType arrayType = (ArrayType) ((PointerType) getType()).getpType();
            if (isZero()) {
                sb.append(": .space ");
                if (arrayType.getElementType().isArrayType())
                    sb.append(arrayType.getSize() * ((ArrayType) arrayType.getElementType()).getSize() * 4);
                else
                    sb.append(arrayType.getSize() * 4);
            } else {
                sb.append(": .word ");
                if (arrayType.getElementType().isArrayType()) {
                    for (int i = 0; i < arrayType.getSize(); i++) {
                        for (int j = 0; j < ((ArrayType) arrayType.getElementType()).getSize(); j++) {
                            sb.append(values.get(i * ((ArrayType) arrayType.getElementType()).getSize() + j).getName());
                            if (j != ((ArrayType) arrayType.getElementType()).getSize() - 1 || i != arrayType.getSize() - 1)
                                sb.append(", ");
                        }
                    }
                } else {
                    for (int i = 0; i < arrayType.getSize(); i++) {
                        sb.append(values.get(i).getName());
                        if (i != arrayType.getSize() - 1) sb.append(", ");
                    }
                }
            }
        } else {
            sb.append(": .word ");
            sb.append(value.getName());
        }
        return sb.toString();
    }
}
