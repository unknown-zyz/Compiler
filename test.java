import java.io.FileWriter;
import java.io.IOException;

public class test{
    public static void main(String[] args) {
        String output = "Hello, World!"; // 将要写入到文件的输出内容

        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write(output); // 将输出内容写入文件
            writer.close(); // 关闭文件写入器
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}