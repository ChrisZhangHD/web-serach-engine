import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {


    private static final int BUFFER_SIZE = 4096; // 4KB
    byte[] buffer = new byte[BUFFER_SIZE];
    public int index = 0;

    public static void main(String[] args) {

        String file = "varbyte.bin";
        int BUFFER_SIZE = 4096; // 4KB
        byte[] buffer = new byte[BUFFER_SIZE];


        try {
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(file);
            Test test = new Test();
            test.varByteEncode(144);
            test.varByteEncode(34);
            test.index = 0;

            outputStream.write(test.buffer);
            inputStream.read(buffer);
            while (test.index < buffer.length) {
                System.out.println(test.VarDecode(buffer));
            }

//            while (inputStream.read(buffer) != -1) {
//
//            }

        } catch (IOException ex) {
            ex.printStackTrace();

        }

//        System.out.println((byte)129);
//        System.out.println();
    }

    public void varByteEncode(int num) {
        while (num > 127) {
            writeByte((byte) (num & 127));
            num = num >> 7;
        }
        writeByte((byte) (128 + num));
    }

    public int VarDecode(byte[] buffer) {
        int val = 0, shift = 0;
        int b = readByte(buffer) & 255;
        while (b < 128) {
            val = val + (b << shift);
            shift = shift + 7;
            b = readByte(buffer) & 255;
        }
        val = val + ((b - 128) << shift);
        return (val);
    }

    public void writeByte(byte byteNum) {
        buffer[index] = byteNum;
        index += 1;
    }

    public int readByte(byte[] buffer) {
        if (index < buffer.length) {
            byte temp = buffer[index];
            index += 1;
            return temp;
        } else {
            return 0;
        }

    }

    public void convert() {
        List<String> itemList = new ArrayList<>();
        itemList.add("item1");
        itemList.add("item2");
        itemList.add("item3");

        String[] itemsArray = new String[itemList.size()];
        itemsArray = itemList.toArray(itemsArray);

        for(String s : itemsArray)
            System.out.println(s);
    }


}
