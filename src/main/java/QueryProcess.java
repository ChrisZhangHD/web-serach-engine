import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class QueryProcess {

    private final Map<String, String[]> lexiconMap;
    private final int maxDocId;
    private final int avgDocLength;

    public QueryProcess() {
        lexiconMap = new HashMap<>();
        maxDocId = FileUtils.getDocCnt();
        String baseInfo = FileUtils.getLineFromFile(FileUtils.PAGE_TABLE_FILE_PATH, maxDocId + 1);
        long allDocLength = Long.parseLong(baseInfo.split(" ")[1]);
        avgDocLength = (int) allDocLength / maxDocId;
    }

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
                System.out.println(getFreq(invertedListObj, 657831));
                System.out.println(maxDocId == nextGEQ(invertedListObj, 3222608));
                System.out.println(getScore(invertedListObj, 669000));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int nextGEQ(InvertedListObj invertedListObj, int k) {
        int[] lastDocIdBlockArray = invertedListObj.getLastDocIdBlockArray();
        if (k > lastDocIdBlockArray[lastDocIdBlockArray.length - 1]) {
            return maxDocId;
        }
        int[] docIdBlockSizeArray = invertedListObj.getDocIdBlockSizeArray();
        int block = binarySearch(lastDocIdBlockArray, k);
        int offset = 0;
        for (int i = 0; i < block; i++) {
            offset += docIdBlockSizeArray[i];
        }
        int docIdStartIndex = invertedListObj.getDocIdStartIndex() + offset;
        int[] docIdBlockArray = invertedListObj.getDocIdBlockArray(docIdStartIndex, block);
        int docIdIndex = binarySearch(docIdBlockArray, k);
        return docIdBlockArray[docIdIndex];
    }

    public int binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (target > arr[mid]) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    public int[] getBlockAndIndex(InvertedListObj invertedListObj, int docId) {
        int[] lastDocIdBlockArray = invertedListObj.getLastDocIdBlockArray();
        int[] docIdBlockSizeArray = invertedListObj.getDocIdBlockSizeArray();
        int block = binarySearch(lastDocIdBlockArray, docId);
        int offset = 0;
        for (int i = 0; i < block; i++) {
            offset += docIdBlockSizeArray[i];
        }
        int docIdStartIndex = invertedListObj.getDocIdStartIndex() + offset;
        int[] docIdBlockArray = invertedListObj.getDocIdBlockArray(docIdStartIndex, block);
        int docIdIndex = binarySearch(docIdBlockArray, docId);
        return new int[]{block, docIdIndex};
    }

    public int getFreq(InvertedListObj invertedListObj, int docId) {
        int[] blockAndIndexArray = getBlockAndIndex(invertedListObj, docId);
        int block = blockAndIndexArray[0];
        int docIdIndex = blockAndIndexArray[1];

        int[] freqBlockSizeArray = invertedListObj.getFreqBlockSizeArray();
        int offset = 0;
        for (int i = 0; i < block; i++) {
            offset += freqBlockSizeArray[i];
        }
        int freqStartIndex = invertedListObj.getFreqStartIndex() + offset;
        int[] freqBlockArray = invertedListObj.getFreqBlockArray(freqStartIndex, block);
        return freqBlockArray[docIdIndex];
    }


    public void closeList(InvertedListObj invertedListObj) {
        invertedListObj = null;
    }

    public double getScore(InvertedListObj invertedListObj, int docId) {
        String basicInfo = FileUtils.getLineFromFile(FileUtils.PAGE_TABLE_FILE_PATH, docId);
        int docLen = Integer.parseInt(basicInfo.split(" ")[2]);

        double K = 1.2 * (0.25 + 0.75 * (1.0 * docLen / avgDocLength));
        int ft = invertedListObj.getPostingCnt();
        int fdt = getFreq(invertedListObj, docId);
        return Math.log((maxDocId - ft + 0.5) / (ft + 0.5)) * ((2.2 * fdt) / (K + fdt));
    }

    public static void main(String[] args) {
        QueryProcess queryProcess = new QueryProcess();
        queryProcess.readFileByLine();
        System.out.println("build success.");
        queryProcess.openList("sulphites");

    }
}
