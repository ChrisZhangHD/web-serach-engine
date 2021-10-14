import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InvertedIndex {

    private final byte[] buffer;
    private int InvertedListStartIndex = 0;
    private String lexiconFileName = "lexicon.txt";
    private final String invertedListFileName = "invertedList.bin";
    private OutputStream lexiconOutputStream;
    private OutputStream invertedListOutputStream;

    public InvertedIndex(int bufferSize) {
        this.buffer = new byte[bufferSize];
        try {
            invertedListOutputStream = new FileOutputStream(invertedListFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    public void varByteEncode(int num) {
//        while (num > 127) {
//            writeByte((byte) (num & 127));
//            num = num >> 7;
//        }
//        writeByte((byte) (128 + num));
//    }

//    public int varByteDecode(byte[] buffer) {
//        int val = 0, shift = 0;
//        int b = readByte(buffer) & 255;
//        while (b < 128) {
//            val = val + (b << shift);
//            shift = shift + 7;
//            b = readByte(buffer) & 255;
//        }
//        val = val + ((b - 128) << shift);
//        return (val);
//    }

//    public void writeByte(byte byteNum) {
//        buffer[index] = byteNum;
//        index += 1;
//    }

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

//    public int readByte(byte[] buffer) {
//        if (index < buffer.length) {
//            byte temp = buffer[index];
//            index += 1;
//            return temp;
//        } else {
//            return 0;
//        }
//
//    }

    public void createBinFile() {
        try{
            File file = new File(invertedListFileName);
            file.createNewFile();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void writeToFile() {

    }

    public void readFileByLine(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line
                handleInvertedList(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleInvertedList(String line) {
        try {
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

//        InvertedList invertedList = new InvertedList(postingCnt, docIdArray, freqArray);
//        generateBlock(invertedList);
            byte[] revertedListByteArray = generateBlock(postingCnt, docIdArray, freqArray);
            int offset = revertedListByteArray.length;
            System.out.println(curWord);
            invertedListOutputStream.write(revertedListByteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] generateBlock(int postingCnt, int[] docIdArray, int[] freqArray) {
        List<Byte> docIdBlockList = new ArrayList<>();
        List<Byte> docIdBlockSizeList = new ArrayList<>();
        List<Byte> blockLastDocIdList = new ArrayList<>();
        List<Byte> freqBlockList = new ArrayList<>();
        List<Byte> freqBlockSizeList = new ArrayList<>();
//        int[] docIdArray = invertedList.getDocIdArray();
//        int[] freqArray = invertedList.getFreqArray();
        int prevDocIdBlockListLen;
        int prevFreqBlockListLen;
        int curDocIdBlockSize = 0;
        int curFreqBlockSize = 0;
        for(int i = 0; i < postingCnt; i ++) {
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
//            int lastDocId = docIdArray[invertedList.getPostingCnt() - 1];
            int lastDocId = docIdArray[postingCnt - 1];
            varByteEncode(lastDocId, blockLastDocIdList);
        }

//        int postingCnt = invertedList.getPostingCnt();
        List<Byte> revertedByteList = new ArrayList<>();
        varByteEncode(postingCnt, revertedByteList);
        revertedByteList.addAll(docIdBlockSizeList);
        revertedByteList.addAll(freqBlockSizeList);
        revertedByteList.addAll(blockLastDocIdList);
        revertedByteList.addAll(docIdBlockList);
        revertedByteList.addAll(freqBlockList);

        return transByteListToArray(revertedByteList);

//        invertedList.setDocIdByteArray(transByteListToArray(docIdBlockList));
//        invertedList.setDocIdBlockSizeArray(transByteListToArray(docIdBlockSizeList));
//        invertedList.setFreqByteArray(transByteListToArray(freqBlockList));
//        invertedList.setFreqBlockSizeArray(transByteListToArray(freqBlockSizeList));
//        invertedList.setBlockLastDocIdArray(transByteListToArray(blockLastDocIdList));


//        invertedList.setDocIdByteArray(docIdBlockList.size());
//        Byte[] docIdByteArray = invertedList.getDocIdByteArray();
//        docIdBlockList.toArray(docIdByteArray);
//
//        invertedList.setDocIdBlockSizeArray(docIdBlockSizeList.size());
//        Byte[] docIdBlockSizeArray = invertedList.getDocIdBlockSizeArray();
//        docIdBlockSizeList.toArray(docIdBlockSizeArray);
//
//        invertedList.setFreqByteArray(freqBlockList.size());
//        Byte[] freqByteArray = invertedList.getFreqByteArray();
//        freqBlockList.toArray(freqByteArray);
//
//        invertedList.setFreqBlockSizeArray(freqBlockSizeList.size());
//        Byte[] freqBlockSizeArray = invertedList.getFreqBlockSizeArray();
//        freqBlockSizeList.toArray(freqBlockSizeArray);
//
//        invertedList.setBlockLastDocIdArray(blockLastDocIdList.size());
//        Byte[] blockLastDocIdArray = invertedList.getBlockLastDocIdArray();
//        blockLastDocIdList.toArray(blockLastDocIdArray);

    }

    public byte[] transByteListToArray(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for(int i = 0; i < list.size(); i ++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

//    public void writeInvertedListToFile(InvertedList invertedList) {
//        try {
//            int offset = 0;
//
////            lexiconOutputStream.write(postingCntArray);
////            offset += postingCntArray.length;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public static void main(String[] args) {
        int bufferSize = 1024 * 1024 * 128;
        InvertedIndex invertedIndex = new InvertedIndex(bufferSize);
        invertedIndex.createBinFile();
        invertedIndex.readFileByLine("aaa.txt");
    }


}
