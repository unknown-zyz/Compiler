package Config;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Config {
    public static final String InputPath = "testfile.txt";
    public static final String TokenPath = "token.txt";
    public static final String OutputPath = "output.txt";
    public static final String ErrorPath = "error.txt";
    public  static final String LLVMPath = "llvm_ir.txt";


    public static final boolean Lexer_Switch = false;
    public static final boolean Parser_Switch = false;
    public static final boolean Error_Switch = true;
    public static final boolean LLVM_Switch = true;

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
