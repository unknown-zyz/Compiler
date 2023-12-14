import Backend.MIPS;
import Midend.LLVM.IRModule;
import Midend.LLVM.Visitor;
import Frontend.Lexical.Lexer;
import Frontend.Lexical.Word;
import Frontend.Syntax.Node.CompUnit;
import Frontend.Syntax.SyntaxMain;
import Midend.LLVM.IROutput;
import Midend.Optimization.Mem2Reg;

import java.io.*;
import java.util.ArrayList;

import static Config.Config.*;
import static Frontend.Syntax.SyntaxMain.*;


public class Compiler {
    public static void main(String[] args) {
        try {
            init();
            BufferedReader br = new BufferedReader(new FileReader(InputPath));
            String str = br.readLine();
            StringBuilder article = new StringBuilder();
            while (str != null) {
                article.append(str);
                article.append('\n');
                str = br.readLine();
            }
            Lexer lexer = new Lexer();
            lexer.analyse(article.toString());
            ArrayList<Word> tokenList = lexer.getTokenList();
            if (Lexer_Switch) lexer.print();

            SyntaxMain.setTokenList(tokenList);
            SyntaxMain syntaxMain = new SyntaxMain();
            syntaxMain.analyse();

            PrintStream originalOut = System.out;

//            FileOutputStream AST_fileOut = new FileOutputStream("AST.txt");
//            PrintStream AST_printOut = new PrintStream(AST_fileOut);
//            System.setOut(AST_printOut);
//            syntaxMain.printAST();
//            AST_printOut.close();
//            AST_fileOut.close();
//
//            printSymbolTable();

            if (isErrorEmpty()) {
                if (Parser_Switch) {
                    FileOutputStream fileOut = new FileOutputStream(OutputPath);
                    PrintStream printOut = new PrintStream(fileOut);
                    System.setOut(printOut);
                    syntaxMain.print();
                    printOut.close();
                    fileOut.close();
                }
                if (LLVM_Switch) {
                    System.setOut(originalOut);
                    IRModule module = new Visitor().visit((CompUnit) syntaxMain.getAST());
                    IROutput.ModuleOutput(module, LLVMPath);

                    //removeUselessBLock待实现
                    if (Optimizer_Switch) {
                        new Mem2Reg().run(module);
                        IROutput.ModuleOutput(module, LLVMOptPath);
                    }

                    if(Mips_Switch) {
                        new MIPS().run(module);
                    }
                }

            } else {
                if (Error_Switch) syntaxMain.printError();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
