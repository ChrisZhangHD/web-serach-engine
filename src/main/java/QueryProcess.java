import java.io.*;
import java.util.*;

public class QueryProcess {

    private final Map<String, String[]> lexiconMap;
    private final String[] pageWebSiteArray;
    private final int[] pageLengthArray;
    private final int maxDocId;
    private final int avgDocLength;
    private final PriorityQueue<DocObj> pq;

    public QueryProcess() {
        lexiconMap = new HashMap<>();
        initLexiconMap();
        pq = new PriorityQueue<>(10, Comparator.comparingDouble(DocObj::getScore));
        maxDocId = FileUtils.getDocCnt();
        pageWebSiteArray = new String[maxDocId + 1];
        pageLengthArray = new int[maxDocId + 1];
        initPageInfo();
        String baseInfo = FileUtils.getLineFromFile(FileUtils.PAGE_TABLE_FILE_PATH, maxDocId + 1);
        long allDocLength = Long.parseLong(baseInfo.split(" ")[1]);
        avgDocLength = (int) (allDocLength / maxDocId);
    }

    public void initLexiconMap() {
        try (BufferedReader br = new BufferedReader(new FileReader(FileUtils.LEXICON_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(" ");
                lexiconMap.put(items[0], new String[]{items[1], items[2]});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initPageInfo() {
        try (BufferedReader br = new BufferedReader(new FileReader(FileUtils.PAGE_TABLE_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(" ");
                if (items.length == 3) {
                    int index = Integer.parseInt(items[0]);
                    pageWebSiteArray[index] = items[1];
                    pageLengthArray[index] = Integer.parseInt(items[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InvertedListObj openList(String word) {
        try {
            String[] invertedListInfo = lexiconMap.get(word);
            long startPosStr = Long.parseLong(invertedListInfo[0]);
            int listLen = Integer.parseInt(invertedListInfo[1]);
            byte[] buffer = new byte[listLen];
            InputStream inputStream = new FileInputStream(FileUtils.INVERTED_LIST_BIN_FILE_PATH);
            long skipBytes = inputStream.skip(startPosStr);
            if (skipBytes == startPosStr && inputStream.read(buffer) != -1) {
                return new InvertedListObj(buffer, word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public double getScore(InvertedListObj invertedListObj, int fdt, int docId) {
        int docLen = pageLengthArray[docId];
        double K = 1.2 * (0.25 + 0.75 * (1.0 * docLen / avgDocLength));

        int ft = invertedListObj.getPostingCnt();
        return Math.log((maxDocId - ft + 0.5) / (ft + 0.5)) * ((2.2 * fdt) / (K + fdt));
    }

    public void conjunctiveSearch(String query) {
        String[] terms = query.split("\\+");
        int n = terms.length;
        InvertedListObj[] lps = new InvertedListObj[terms.length];
        for (int i = 0; i < n; i++) {
            lps[i] = openList(terms[i]);
        }
        Arrays.sort(lps, Comparator.comparingInt(InvertedListObj::getPostingCnt));
        int docId = 0;
        while (docId <= maxDocId) {
            /* get next post from shortest list */
            docId = nextGEQ(lps[0], docId);
            if (docId == maxDocId) {
                break;
            }
            /* see if you find entries with same docID in other lists */
            int tempDocId = 0;
            for (int i = 1; i < n; i++) {
                tempDocId = nextGEQ(lps[i], docId);
                if (tempDocId != docId) {
                    break;
                }
            }
            if (tempDocId > docId) {
                /* not in intersection */
                docId = tempDocId;
            } else {
                /* docID is in intersection; now get all frequencies */
                double score = 0;
                DocObj docObj = new DocObj(docId, lps.length, pageWebSiteArray[docId]);
                String[] docObjWords = docObj.getWords();
                int[] docObjWordsFreq = docObj.getWordsFreq();
                for (int i = 0; i < n; i++) {
                    int freq = getFreq(lps[i], docId);
                    score += getScore(lps[i], freq, docId);
                    /* compute BM25 score from frequencies and other data */
                    docObjWords[i] = lps[i].getWord();
                    docObjWordsFreq[i] = freq;
                }
                docObj.setScore(score);
                pq.add(docObj);
                if (pq.size() > 10) {
                    pq.poll();
                }
                /* and increase did to search for next post */
                docId++;
            }
        }
        for (int i = 0; i < n; i++) {
            closeList(lps[i]);
        }
    }

    public void getTop10Res() {
        String[] results = new String[10];
        int index = results.length - 1;
        while (pq.size() > 0) {
            DocObj docObj = pq.poll();
            StringBuilder result = new StringBuilder(docObj.getDocId() + " " + docObj.getDocLink() + " " + (docObj.getScore()));
            String[] docObjWords = docObj.getWords();
            int[] docObjWordsFreq = docObj.getWordsFreq();
            result.append(" ( ");
            for (int i = 0; i < docObjWords.length; i++) {
                result.append(docObjWords[i]).append(":").append(docObjWordsFreq[i]).append(" ");
            }
            result.append(")");
            results[index] = result.toString();
            index--;
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(results[i]);
        }
    }

    public static void main(String[] args) {
        QueryProcess queryProcess = new QueryProcess();
        System.out.println("build success.");
        long startTime = System.currentTimeMillis();
        queryProcess.conjunctiveSearch("apple+iphone");
//        queryProcess.conjunctiveSearch("brooklyn+park");
        queryProcess.getTop10Res();
        long endTime = System.currentTimeMillis();
        System.out.println("Run Time： " + (endTime - startTime) + "ms");
    }
}
