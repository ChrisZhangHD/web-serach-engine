import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryProcess {

    private final Map<String, String[]> lexiconMap = new HashMap<>();
    private static final int MAX_DOC_ID = 3222846;

    public void buildLexiconMap(String line) {
        String[] item = line.split(" ");
        lexiconMap.put(item[0], new String[]{item[1], item[2]});
    }

    public void readFileByLine() {
        try (BufferedReader br = new BufferedReader(new FileReader(FileUtils.LEXICON_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                buildLexiconMap(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public void openList(String word) {
//        try {
//            String[] invertedListInfo = lexiconMap.get(word);
//            long startPosStr = Long.parseLong(invertedListInfo[0]);
//            int listLen = Integer.parseInt(invertedListInfo[1]);
//            byte[] buffer = new byte[listLen];
//            InputStream inputStream = new FileInputStream(FileUtils.INVERTED_LIST_BIN_FILE_PATH);
//            long skipBytes = inputStream.skip(startPosStr);
//            if (skipBytes == startPosStr && inputStream.read(buffer) != -1) {
//                int[] index = new int[1];
//                List<Integer> invertedList = new ArrayList<>();
//                while(index[0] < listLen) {
//                    int val = varByteDecode(buffer, index);
//                    invertedList.add(val);
//                }
//                System.out.println(invertedList.toString());
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void openList(String word) {
        try {
            String[] invertedListInfo = lexiconMap.get(word);
            long startPosStr = Long.parseLong(invertedListInfo[0]);
            int listLen = Integer.parseInt(invertedListInfo[1]);
            byte[] buffer = new byte[listLen];
            InputStream inputStream = new FileInputStream(FileUtils.INVERTED_LIST_BIN_FILE_PATH);
            long skipBytes = inputStream.skip(startPosStr);
            if (skipBytes == startPosStr && inputStream.read(buffer) != -1) {
                InvertedListObj invertedListObj = new InvertedListObj(buffer);
                System.out.println("---------");
                System.out.println(nextGEQ(invertedListObj, 1991246));
                System.out.println(nextGEQ(invertedListObj, 669294));
                System.out.println(nextGEQ(invertedListObj, 669000));
                System.out.println(MAX_DOC_ID == nextGEQ(invertedListObj, 3219839));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public int varByteDecode(byte[] buffer, int[] index) {
//        int val = 0, shift = 0;
//        int b = buffer[index[0]] & 255;
//        while (b < 128) {
//            val = val + (b << shift);
//            shift = shift + 7;
//            index[0] += 1;
//            b = buffer[index[0]] & 255;
//        }
//        val = val + ((b - 128) << shift);
//        index[0] += 1;
//        return (val);
//    }

//    public int varByteDecode(byte[] buffer, int[] index) {
//        int val = 0, shift = 0;
//        int b = readByte(buffer, index) & 255;
//        while (b < 128) {
//            val = val + (b << shift);
//            shift = shift + 7;
//            b = readByte(buffer, index) & 255;
//        }
//        val = val + ((b - 128) << shift);
//        return (val);
//    }


//    public int readByte(byte[] buffer, int[] index) {
//        if (index[0] < buffer.length) {
//            return buffer[index];
//        } else {
//            return 0;
//        }
//
//    }

    public int nextGEQ(InvertedListObj invertedListObj, int k) {
        int[] lastDocIdBlockArray = invertedListObj.getLastDocIdBlockArray();
        if(k > lastDocIdBlockArray[lastDocIdBlockArray.length - 1]) {
            return MAX_DOC_ID;
        }
        int[] docIdBlockSizeArray = invertedListObj.getDocIdBlockSizeArray();
        int block = binarySearch(lastDocIdBlockArray, k);
        int offset = 0;
        for(int i = 0; i < block; i ++) {
            offset += docIdBlockSizeArray[i];
        }
        int docIdStartIndex = invertedListObj.getDocIdStartIndex() + offset;
        int[] docIdBlockArray = invertedListObj.getDocIdBlockArray(docIdStartIndex, block);
        int docIdIndex = binarySearch(docIdBlockArray, k);
        return docIdBlockArray[docIdIndex];
    }

    public int binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length;
        while(left < right) {
            int mid = left + (right - left) / 2;
            if(target > arr[mid]) {
                left = mid + 1;
            }else{
                right = mid;
            }
        }
        return left;
    }


    public void closeList() {

    }

    public void getScore() {

    }

    public static void main(String[] args) {
        QueryProcess queryProcess = new QueryProcess();
        queryProcess.readFileByLine();
        System.out.println("build success.");
        queryProcess.openList("sulphites");

    }
}
