package Backend;

import Midend.LLVM.Value.Value;

public class Memory {
    private String addr;
    private Integer off;
    private Value value;
    private Reg offReg;

    public Memory(String addr, Integer off, Value value) {
        this.addr = addr;
        this.off = off;
        this.value = value;
    }

    public Memory(String addr, Reg offReg, Value value) {
        this.addr = addr;
        this.offReg = offReg;
        this.value = value;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Integer getOff() {
        return off;
    }

    public Reg getOffReg() {
        return offReg;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

}
