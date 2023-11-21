package Config;

import java.io.File;

public class Config {
    public static final String InputPath = "testfile.txt";
    public static final String TokenPath = "token.txt";
    public static final String OutputPath = "output.txt";
    public static final String ErrorPath = "error.txt";
    public static final String LLVMPath = "llvm_ir_no_opt.txt";
    public static final String LLVMOptPath = "llvm_ir.txt";


    public static final boolean Lexer_Switch = false;
    public static final boolean Parser_Switch = false;
    public static final boolean Error_Switch = true;
    public static final boolean LLVM_Switch = true;
    public static final boolean Optimizer_Switch = true;

    public static void delete(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void init() {
        delete(TokenPath);
        delete(OutputPath);
        delete(ErrorPath);
        delete(LLVMPath);
    }

}
