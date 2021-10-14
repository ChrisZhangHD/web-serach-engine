import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvertedIndex {

    private final byte[] buffer;
    private int index = 0;

    public InvertedIndex(int bufferSize) {
        this.buffer = new byte[bufferSize];
    }

    public void varByteEncode(int num) {
        while (num > 127) {
            writeByte((byte) (num & 127));
            num = num >> 7;
        }
        writeByte((byte) (128 + num));
    }

    public int varByteDecode(byte[] buffer) {
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

    public void varByteEncode(int num, List<Byte> arrayList) {
        while (num > 127) {
            writeByteToArrayList((byte) (num & 127), arrayList);
            num = num >> 7;
        }
        writeByteToArrayList((byte) (128 + num), arrayList);
    }

    public void writeByteToArrayList(byte byteNum, List<Byte> arrayList) {
        arrayList.add(byteNum);
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

    public void createBinFile() {
        try{
            File file = new File("invertedList.bin");
            file.createNewFile();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void readFileByLine(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            if ((line = br.readLine()) != null) {
                handleInvertedList(line);
                // process the line.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleInvertedList(String line) {
        String[] array = line.split("-");
        String curWord = array[0];
        String[] postingArray = array[1].split("#");
        int postingCnt = postingArray.length;
        int[] docIdArray = new int[postingCnt];
        int[] freqArray = new int[postingCnt];
        for (int i = 0; i < postingArray.length; i ++){
            String[] tempArray = postingArray[i].split(":");
            int docId = Integer.parseInt(tempArray[0]);
            int freq = Integer.parseInt(tempArray[1]);
            docIdArray[i] = docId;
            freqArray[i] = freq;
        }
        InvertedList invertedList = new InvertedList(postingCnt, docIdArray, freqArray);
        generateBlock(invertedList);
        System.out.println(curWord);
    }

    public void generateBlock(InvertedList invertedList) {
        List<Byte> docIdBlockList = new ArrayList<>();
        List<Byte> docIdBlockSizeList = new ArrayList<>();
        List<Byte> blockLastDocIdList = new ArrayList<>();
        List<Byte> freqBlockList = new ArrayList<>();
        List<Byte> freqBlockSizeList = new ArrayList<>();
        int[] docIdArray = invertedList.getDocIdArray();
        int[] freqArray = invertedList.getFreqArray();
        int prevDocIdBlockListLen;
        int prevFreqBlockListLen;
        int curDocIdBlockSize = 0;
        int curFreqBlockSize = 0;
        for(int i = 0; i < invertedList.getPostingCnt(); i ++) {
            // ==============================
            int curDocId = docIdArray[i];
            prevDocIdBlockListLen = docIdBlockList.size();
            varByteEncode(curDocId, docIdBlockList);
            curDocIdBlockSize += docIdBlockList.size() - prevDocIdBlockListLen;

            // ==============================
            int freq = freqArray[i];
            prevFreqBlockListLen = freqBlockList.size();
            varByteEncode(freq, freqBlockList);
            curFreqBlockSize += freqBlockList.size() - prevFreqBlockListLen;

            if ((i + 1) % 64 == 0) {
                varByteEncode(curDocIdBlockSize, docIdBlockSizeList);
                varByteEncode(curFreqBlockSize, freqBlockSizeList);
                varByteEncode(curDocId, blockLastDocIdList);
                curDocIdBlockSize = 0;
                curFreqBlockSize = 0;
            }
        }
        if(curDocIdBlockSize != 0) {
            varByteEncode(curDocIdBlockSize, docIdBlockSizeList);
            varByteEncode(curFreqBlockSize, freqBlockSizeList);
            int lastDocId = docIdArray[invertedList.getPostingCnt() - 1];
            varByteEncode(lastDocId, blockLastDocIdList);
        }
    }

    public static void main(String[] args) {
        int bufferSize = 1024 * 1024 * 128;
        InvertedIndex invertedIndex = new InvertedIndex(bufferSize);
//        invertedIndex.createBinFile();
        invertedIndex.readFileByLine("aaa.txt");
    }


}
