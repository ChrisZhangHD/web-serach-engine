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
                List<String> docObjWords = docObj.getWords();
                List<Integer> docObjWordsFreq = docObj.getWordsFreq();
                for (int i = 0; i < n; i++) {
                    int freq = getFreq(lps[i], docId);
                    score += getScore(lps[i], freq, docId);
                    /* compute BM25 score from frequencies and other data */
                    docObjWords.add(lps[i].getWord());
                    docObjWordsFreq.add(freq);
                }
                docObj.addScore(score);
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

    public void disjunctiveSearch(String query) {
        String[] terms = query.split("\\+");
        int n = terms.length;
        InvertedListObj[] lps = new InvertedListObj[terms.length];
        Map<Integer, DocObj> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            lps[i] = openList(terms[i]);
            updateDocIdScoreMap(lps[i], map);
        }
        for (int key : map.keySet()) {
            pq.add(map.get(key));
            if (pq.size() > 10) {
                pq.poll();
            }
        }

    }

    private void updateDocIdScoreMap(InvertedListObj invertedListObj, Map<Integer, DocObj> map) {
        int blockCnt = invertedListObj.getBlockCnt();
        int docIdStart = invertedListObj.getDocIdStartIndex();
        int freqStart = invertedListObj.getFreqStartIndex();
        int[] docIdBlockSizeArray = invertedListObj.getDocIdBlockSizeArray();
        int[] freqBlockSizeArray = invertedListObj.getFreqBlockSizeArray();
        String curWord = invertedListObj.getWord();
        for (int i = 0; i < blockCnt; i++) {
            int[] docIdArray = invertedListObj.getDocIdBlockArray(docIdStart, i);
            int[] freqArray = invertedListObj.getFreqBlockArray(freqStart, i);
            for (int j = 0; j < docIdArray.length; j++) {
                int curDocId = docIdArray[j];
                int curFreq = freqArray[j];
                if (!map.containsKey(curDocId)) {
                    DocObj docObj = new DocObj(curDocId, pageWebSiteArray[curDocId]);
                    map.put(curDocId, docObj);
                }
                DocObj docObj = map.get(curDocId);
                docObj.getWords().add(curWord);
                docObj.getWordsFreq().add(curFreq);
                double score = getScore(invertedListObj, curFreq, curDocId);
                docObj.addScore(score);
            }
            docIdStart += docIdBlockSizeArray[i];
            freqStart += freqBlockSizeArray[i];
        }
    }

    public void getTop10Res() {
        String[] results = new String[10];
        int index = results.length - 1;
        while (pq.size() > 0) {
            DocObj docObj = pq.poll();
            StringBuilder result = new StringBuilder(docObj.getDocId() + " " + docObj.getDocLink() + " " + (docObj.getScore()));
            List<String> docObjWords = docObj.getWords();
            List<Integer> docObjWordsFreq = docObj.getWordsFreq();
            result.append(" ( ");
            for (int i = 0; i < docObjWords.size(); i++) {
                result.append(docObjWords.get(i)).append(":").append(docObjWordsFreq.get(i)).append(" ");
            }
            result.append(")");
            results[index] = result.toString();
            index--;
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(results[i]);
        }
        pq.clear();
    }

    public static void main(String[] args) {

        QueryProcess queryProcess = new QueryProcess();
        System.out.println("build success.");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Input search mode (and / or): ");
            String mode = scanner.nextLine();
            System.out.print("Input key words: ");
            String keywords = scanner.nextLine();
            long startTime = System.currentTimeMillis();
            if (mode.equals("and")) {
                queryProcess.conjunctiveSearch(keywords);
            } else {
                queryProcess.disjunctiveSearch(keywords);
            }
            queryProcess.getTop10Res();
            long endTime = System.currentTimeMillis();
            System.out.println("Run Time： " + (endTime - startTime) + "ms");
        }
    }
}
