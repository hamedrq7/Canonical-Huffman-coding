import java.io.*;
import java.util.*;

public class main {
    public static void main(String[] args) throws IOException {

        File textFile = new File("b.txt");
        Scanner textFileScanner = new Scanner(textFile);
        StringBuilder text = new StringBuilder();
        while (textFileScanner.hasNextLine()) {
            text.append(textFileScanner.nextLine());
            if(textFileScanner.hasNextLine()) text.append("\n");
        }
        System.out.println(text);


        PriorityQueue<HuffManCode> hmc = encode_HuffMan(text.toString());

        //hmc contains all huffManCodes, we use it in order to
        //get to Canonical HuffManCodes
        //we also use mean heap because we need codes to be sorted
        //by their code.length and lexicographical

        HashMap<Character, String> canonicalMap = encode_Canonical(hmc);
        System.out.println(canonicalMap);

        writeBinaryFile(text.toString(), canonicalMap);

    }


    public static String writeBit(String binaryString, String buffer, DataOutputStream dos) throws IOException {
        buffer += binaryString;
        //System.out.println("before:  " + buffer);
        while(buffer.length()>=8) {
            int intToWrite = Integer.parseInt(buffer, 0, 8, 2);
            //System.out.println("toWrite: " + toBinary(intToWrite, 8));
            //System.out.println(intToWrite);
            dos.write(intToWrite);
            buffer = buffer.substring(8);
        }
        //System.out.println("after:   " +  buffer);
        return buffer;
    }
    public static void writeBinaryFile(String text, HashMap<Character, String> canonicalMap) throws IOException {
        String buffer = "";
        FileOutputStream fos = new FileOutputStream("EncodeSample.bin");
        DataOutputStream dos = new DataOutputStream(fos);

        int maxCanonicalLength = 0;
        int firstAscii = Integer.MAX_VALUE;
        int lastAscii = 0;
        for(Map.Entry<Character, String> x : canonicalMap.entrySet()) {
            firstAscii = Integer.min(firstAscii, (int)x.getKey());
            lastAscii = Integer.max(lastAscii, (int)x.getKey());
            maxCanonicalLength = Integer.max(maxCanonicalLength, x.getValue().length());
        }
        System.out.println("maxConincalLength: " + maxCanonicalLength);
        int maxNumBit = Integer.toBinaryString(maxCanonicalLength).length();

        System.out.println("maxNumBit: "+maxNumBit);
        System.out.println("f: "+firstAscii);
        System.out.println("l: "+lastAscii);

        buffer = writeBit(toBinary(maxNumBit, 4), buffer, dos);
        buffer = writeBit(toBinary(firstAscii, 8), buffer, dos);
        buffer = writeBit(toBinary(lastAscii, 8), buffer, dos);

        //peymane = maxLengthBit
        for(int i = firstAscii; i <= lastAscii; i++) {
            if(canonicalMap.containsKey((char)i)) {
                int currLength = canonicalMap.get((char)i).length();
                buffer = writeBit(toBinary(currLength, maxNumBit), buffer, dos);
            }
            else {
                buffer = writeBit(toBinary(0, maxNumBit), buffer, dos);
            }
        }


        //body:
        for(int i = 0; i < text.length(); i++) {
            String codeToWrite = canonicalMap.get(text.charAt(i));
            buffer = writeBit(codeToWrite, buffer, dos);
        }

        //flush buffer:
        int invalidBitCnt = 0;
        while(buffer.length()<8) {
            buffer += "0";
            invalidBitCnt++;
        }
        dos.write(Integer.parseInt(buffer, 2));
        //0ne byte for invalid bits:
        dos.write(invalidBitCnt);

    }
    public static HashMap<Character, String> encode_Canonical(PriorityQueue<HuffManCode> hmc) {
        HashMap<Character, String> canonicalMap = new HashMap<>();

        int lastCodeLength = 0;
        int currValue = 0;

        while(!hmc.isEmpty()) {
            HuffManCode currHMC = hmc.deletePq();
            //System.out.print(currHMC.getCharacter()+" ");
            String canCode = "";
            int sizeDiff = currHMC.getCodeWord().length() - lastCodeLength;
            currValue = currValue << sizeDiff;
            canCode = toBinary(currValue, currHMC.getCodeWord().length());

            canonicalMap.put(currHMC.getCharacter(), canCode);
            lastCodeLength = canCode.length();
            currValue++;
        }
        return canonicalMap;
    }
    public static PriorityQueue<HuffManCode> encode_HuffMan(String text) {

        //In order to make the Huffman Tree, first we need to have frequency of each word:
        //we use a hashMap to map store frequency of each word
        // ** words with 0 frequency are not  inserted in hashMap**
        HashMap<Character, Integer> dict = new HashMap<Character, Integer>();

        // if test = "HASANabi", hasMap looks like this:
        // ('H', 1), ('A', 2), ('S', 1), ('N', 1), ('a', 1), ('b', 1), ('i', 1)
        for(int i = 0; i < text.length(); i++) {
            if(dict.containsKey(text.charAt(i))) {
                dict.replace(text.charAt(i), dict.get(text.charAt(i)), dict.get(text.charAt(i))+1);
            }
            else {
                dict.put(text.charAt(i), 1);
            }
        }

        // making the priority queue:
        // not necessary
        PriorityQueue<CharFreq> frequencies = new PriorityQueue<>();
        for(Map.Entry<Character, Integer> x : dict.entrySet()) {
            CharFreq cf = new CharFreq(x.getKey(), x.getValue());
            frequencies.addPq(cf);
        }

        // Algorithm to make a huffMan tree of words,
        // knowing their frequency can be implemented like this:
        // (use a priority queue(that uses mean heap)
        // and store every word and their frequency as a node in that queue)

        // 1-pop two element (both have lowest value, since it uses mean heap
        // 2-make a new huffManNode that has value of 1stElement.value + 2ndElement.value --> C
        // 3-third huffManNode(C) is the parent of two other node in huffManTree
        //      ( C )
        //      /   \
        //    (A)   (B)
        // 4-add new huffman node to the queue
        // 5-repeat from 1st step, until #element in priority queue is less than two, then
        // make a huffManNode with last element in priority queue, this node is the root of HuffMan tree.


        // Making the huffManTree:
        PriorityQueue<HuffmanNode> huffManQueue = new PriorityQueue<>();
        for(Map.Entry<Character, Integer> x : dict.entrySet()) {
            HuffmanNode hmn = new HuffmanNode(x.getKey(), x.getValue());
            huffManQueue.addPq(hmn);
        }

        while(huffManQueue.getLength()>1) {
            HuffmanNode x1 = huffManQueue.deletePq();
            HuffmanNode x2 = huffManQueue.deletePq();

            HuffmanNode x3 = new HuffmanNode(x1.getValue()+x2.getValue());

            x3.setRightChild(x1);
            x3.setLeftChild(x2);
            x1.setParent(x3);
            x2.setParent(x3);

            huffManQueue.addPq(x3);
        }

        // last element that remained in huffManQueue is the root of huff man tree

        //////// now we have huffman tree, its node is node above
        //////// using inorder traversal we can make derive codeWord of each character
        PriorityQueue<HuffManCode> hmc = new PriorityQueue<>();
        inorder(huffManQueue.deletePq(), "", hmc);

        hmc.show();
        //hmc contains huffman code :)
        return hmc;
    }


    public static void inorder(HuffmanNode p, String s, PriorityQueue<HuffManCode> hmc) {
        if(p!=null) {
            if(p.getRightChild()==null&&p.getLeftChild()==null) {
                HuffManCode hCode = new HuffManCode(p.getCharacter(), s.length(), s);
                hmc.addPq(hCode);
                System.out.println(p.getCharacter() + ": " + s);
            }
            inorder(p.getLeftChild(), s+"0", hmc);
            //System.out.print("("+p.getCharacter()+", "+p.getFreq()+") ");
            inorder(p.getRightChild(), s+"1", hmc);
        }
    }

    public static String toBinary(int x, int len) {

        if (len > 0) {
            return String.format("%" + len + "s",
                    Integer.toBinaryString(x)).replaceAll(" ", "0");
        }

        return null;
    }
}

class HuffManCode implements Comparable<HuffManCode> {
    private char character;
    private int value;
    private String codeWord;

    public char getCharacter() {
        return character;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public HuffManCode(char character, int value, String codeWord) {
        this.character = character;
        this.value = value;
        this.codeWord = codeWord;
    }

    @Override
    public String toString() {
        String s = "";
        s += character;
        s+= ", ";
        s+= value;
        s+=", ";
        s+= codeWord;
        return s;
    }

    @Override
    public int compareTo(HuffManCode o) {
        if(this.value>o.value) {
            return 1;
        }
        else if(this.value<o.value) {
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
class HuffmanNode implements Comparable<HuffmanNode>{
    //use huffman node in the commented block of code
    private HuffmanNode rightChild;
    private HuffmanNode leftChild;
    private HuffmanNode parent;
    private char character;
    private int value;
    private String codeWord;
    private int codeLength;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    // default constructor is for intermediate nodes
    // which do not represent any character
    public HuffmanNode() {}
    public HuffmanNode(char character, int value) {
        this.character = character;
        this.value = value;
    }
    public HuffmanNode(int val) {
        this.value = val;
    }
    public HuffmanNode(char character) {
        this.character = character;
    }

    public HuffmanNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(HuffmanNode rightChild) {
        this.rightChild = rightChild;
    }

    public HuffmanNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(HuffmanNode leftChild) {
        this.leftChild = leftChild;
    }

    public HuffmanNode getParent() {
        return parent;
    }

    public void setParent(HuffmanNode parent) {
        this.parent = parent;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public void setCodeWord(String codeWord) {
        this.codeWord = codeWord;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        if(this.value>o.value) {
            return 1;
        }
        else if(this.value<o.value) {
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

    @Override
    public String toString() {
        String s = "";
        s += character;
        s+= ", ";
        s+= value;
        s+=", ";
        s+= codeWord;
        return s;
    }
}

class CharFreq implements Comparable<CharFreq> {
    private char character;
    private int freq;

    public CharFreq(char character, int freq) {
        this.character = character;
        this.freq = freq;
    }

    public char getCharacter() {
        return this.character;
    }
    public int getFreq() {
        return this.freq;
    }


    @Override
    public int compareTo(CharFreq o) {
        if(this.freq>o.freq) {
            return 1;
        }
        else if(this.freq<o.freq) {
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += character;
        s += ", ";
        s += freq;
        return s;
    }
}



class PriorityQueue <T extends Comparable<T>> {

    private int end = 0;
    ArrayList<T> heap = new ArrayList<>();

    public PriorityQueue() {
        this.heap.add(null);
    }


    public <E extends Comparable<E>> void addPq(E newElement) {
        int index = ++end;
        heap.add(null);
        while(index!=1) {
            if(newElement.compareTo((E) heap.get(index/2)) > 0) { // if newElement is bigger
                break;
            }
            //if(index>=heap.size()) {
            //heap.add(heap.get(index/2));
            //} else {
            heap.set(index, heap.get(index/2));
            //}
            index=index/2;
        }
        //if(index >= heap.size()) {
        //    heap.add((T) newElement);
        //}
        //else {
        heap.set(index, (T) newElement);
        //}
    }

    public T deletePq() {
        if(isEmpty()) return null;
        else {
            T result = heap.get(1);
            heap.set(1, heap.get(end));
            heap.set(end, null);
            end--;

            int index = 1;

            while(2*index <= end || 2*index+1 <= end) {

                if(2*index+1>end) {
                    //No Right Child:
                    if(heap.get(index*2).compareTo(heap.get(index))>=0) {
                        //we good
                        break;
                    }
                    else {
                        //parent > right
                        //swap right with parent
                        T temp = heap.get(index);
                        heap.set(index, heap.get(index*2));
                        heap.set(index*2, temp);
                        index=index*2;
                    }
                }
                else {
                    //Right Child And Left Child exist:

                    //if(left >= right)
                    if(heap.get(index*2).compareTo(heap.get(index*2+1))>=0) {
                        //if(right>=parent)
                        if(heap.get(index*2+1).compareTo(heap.get(index))>=0) {
                            //we good
                            break;
                        }
                        else {
                            //parent > right
                            //swap right with parent
                            T temp = heap.get(index);
                            heap.set(index, heap.get(index*2+1));
                            heap.set(index*2+1, temp);
                            index=index*2+1;
                        }
                    }
                    else {
                        //right > left
                        if(heap.get(index*2).compareTo(heap.get(index))>=0) {
                            //we good
                            break;
                        }
                        else {
                            //parent > right
                            //swap right with parent
                            T temp = heap.get(index);
                            heap.set(index, heap.get(index*2));
                            heap.set(index*2, temp);
                            index=index*2;
                        }
                    }
                }
            }
            return result;
        }
    }

    public boolean isEmpty() {
        return (end<1);
    }

    public void show() {
        for(int i = 1; i <= end; i++) {
            System.out.print("("+heap.get(i).toString()+") ");
        }
        System.out.println();
    }

    public int getLength() {
        return end;
    }
}
