import Frontend.Lexical.TokenType;
import Frontend.Lexical.Word;
import Frontend.Syntax.Node.*;
import Midend.LLVM.Visitor;

import java.io.*;

public class test{

    public static void main(String[] args) {
        // 源文件和目标文件的路径
        String sourceFilePath = "1.txt";
        String targetFilePath = "2.txt";



        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(targetFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String modifiedLine = "111";
                System.out.println("m= "+modifiedLine);
                writer.write(modifiedLine);
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}