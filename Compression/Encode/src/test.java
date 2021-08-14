import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class test {
    public static void main(String[] args) throws IOException {
        File newFile = new File("sampleGen.txt");
        FileOutputStream fos = new FileOutputStream(newFile);
        DataOutputStream dos = new DataOutputStream(fos);

        Random r = new Random();
        String text = new String();
        for(int i = 0; i <= 255; i++) {
            int randomFreq = r.nextInt(150)+1;
            for(int j = 0; j < randomFreq; j++) {
                text += String.valueOf((char)i);
            }
        }

        System.out.println(text);
        dos.writeChars(text);
    }
}

