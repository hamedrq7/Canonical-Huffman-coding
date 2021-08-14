import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Decode {
    public static void main(String[] args) throws IOException {

        File file = new File("EncodeSample.bin");
        System.out.println("file size: " + file.length() + " B");
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = dis.readAllBytes();
        System.out.println(bytes.length);

        //convert all bytes into string(maximum size of string won't reach)
        StringBuilder bitsFast = new StringBuilder(bytes.length*8);

        for(int i = 0; i < bytes.length; i++) {
            bitsFast.append(toBinary(Byte.toUnsignedInt(bytes[i]), 8));
        }
        System.out.println(bitsFast.length());



        //reading header and deriving the length of canonical code of each word
        int maxNumBit = Integer.parseInt(bitsFast.substring(0, 4), 2); //first 4bits
        int firstAscii = Integer.parseInt(bitsFast.substring(4, 12), 2); //next byte
        int lastAscii = Integer.parseInt(bitsFast.substring(12, 20), 2); //next byte
        System.out.println("maxNumBit: "+maxNumBit);
        System.out.println("f: "+firstAscii);
        System.out.println("l: "+lastAscii);

        //deriving the canonical length of each character and putting it into pq:
        PriorityQueue<CanonicalLength> clpq = new PriorityQueue<CanonicalLength>();
        //peymane = maxNumBit
        int index = 20;
        int currAscii = firstAscii;
        while(currAscii <= lastAscii) {
            Character currChar = (char)currAscii;
            int currCodeLength = Integer.parseInt(bitsFast.substring(index, index+maxNumBit), 2);
            //System.out.println("char: "+currChar);
            //System.out.println("CodeLength: " + currCodeLength);
            if(currCodeLength!=0) {
                clpq.addPq(new CanonicalLength(currChar, currCodeLength));
            }
            index+=maxNumBit;
            currAscii++;
        }

        clpq.show();
        //"?"?"?"?"
        HashMap<String, Character> canonicalMap = getCanonicalCodeFromLength(clpq);
        System.out.println(canonicalMap);

        //now decoding the body:
        int invalidBitsNum = Integer.parseInt(bitsFast.substring(bitsFast.length()-8), 2)+8;
        String decodedText = bitsFast.substring(index, bitsFast.length()-invalidBitsNum);

        String buffer = "";
        StringBuilder encodedText = new StringBuilder(bitsFast.length());
        for(int i = 0; i < decodedText.length(); i++) {
            buffer += decodedText.charAt(i);
            if(canonicalMap.containsKey(buffer)) {
                encodedText.append(canonicalMap.get(buffer));
                buffer = "";
            }
        }

        //System.out.println(encodedText);

        File output = new File("encodedSample.txt");
        FileOutputStream fos = new FileOutputStream(output);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.writeBytes(encodedText.toString());
    }

    public static HashMap<String, Character> getCanonicalCodeFromLength(PriorityQueue<CanonicalLength> clpq) {
        //we store canonical codes in this hashMap:
        HashMap<String, Character> canonicalMap = new HashMap<>();

        //process of making canonical codes from their length:
        int value = 0;
        int lastBitSize = 0;

        while(!clpq.isEmpty()) {
            String canonicalCode = "";
            CanonicalLength x = clpq.deletePq();
            int sizeDiff = x.getCodeLength()-lastBitSize;
            value = value << sizeDiff;
            canonicalCode = toBinary(value, x.getCodeLength());
            canonicalMap.put(canonicalCode, x.getCharacter());

            value++;
            lastBitSize = x.getCodeLength();
        }

        return canonicalMap;
    }

    public static String toBinary(int x, int len) {

        if (len > 0) {
            return String.format("%" + len + "s",
                    Integer.toBinaryString(x)).replaceAll(" ", "0");
        }

        return null;
    }


}

class CanonicalLength implements Comparable<CanonicalLength>{
    private char character;
    private int codeLength;
    public char getCharacter() {return character; }
    public int getCodeLength() {return codeLength; }

    public CanonicalLength(char ch, int length) {
        this.character = ch;
        this.codeLength = length;
    }

    @Override
    public String toString() {
        String s = "";
        s += character;
        s += " ";
        s += codeLength;
        return s;
    }

    @Override
    public int compareTo(CanonicalLength o) {
        if(this.codeLength>o.codeLength) {
            return 1;
        }
        else if(this.codeLength<o.codeLength) {
            return -1;
        }
        else {
            if((int)this.character>(int)o.character) {
                return 1;
            }
            else if((int)this.character<(int)o.character) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }
}