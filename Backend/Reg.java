package Backend;

public class Reg {
    private String name;
    private boolean used;

    public Reg(String name) {
        this.name = name;
        this.used = false;
    }

    public String getName() {
        return name;
    }

    public void use() {
        this.used = true;
    }

    public void clear() {
        this.used = false;
    }

    public boolean isUsed() {
        return used;
    }
}
