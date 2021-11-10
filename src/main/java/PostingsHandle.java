import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostingsHandle {

    private final String regex = "[A-Za-z]+[0-9]*$";
    private final Pattern p = Pattern.compile(regex);
    private final Map<String, StringBuilder> word2DocIdFreqListMap = new HashMap<>();
    private final BufferedWriter pageTableWriter;
    private final OutputStream docTextOutputStream;
    private long allDocLength = 0L;
    private int docId = 0;
    private long docTextIndex = 0;

    public PostingsHandle() throws IOException {
        this.pageTableWriter = new BufferedWriter(new FileWriter(FileUtils.PAGE_TABLE_FILE_PATH));
        this.docTextOutputStream = new FileOutputStream(FileUtils.DOC_TEXT_FILE);
    }

    public void buildDocPosingIndex(String docText) throws IOException {
        byte[] textByteArray = docText.getBytes();
        int textByteLen = textByteArray.length;
        String[] docTextArray = docText.split(" ");
        int docLength = docTextArray.length - 1;
        if (docLength == 0) {
            return;
        }
        docId += 1;
        Map<String, Integer> word2FreqMap = new HashMap<>();
        String curWord;
        String website = docTextArray[0];
        allDocLength += docLength;
        docTextOutputStream.write(textByteArray);
        pageTableWriter.write(docId + " " + website + " " + docLength + " " + docTextIndex + " " + textByteLen + "\n");
        docTextIndex += textByteLen;
        for (int i = 1; i < docTextArray.length; i++) {
            curWord = docTextArray[i];
            // if the length of word more than 20. Omit it.
            if (curWord.length() > 20) {
                continue;
            }
            Matcher isValid = p.matcher(curWord);
            if (isValid.matches()) {
                curWord = curWord.toLowerCase();
                word2FreqMap.put(curWord, word2FreqMap.getOrDefault(curWord, 0) + 1);
            }
        }
        for (String word : word2FreqMap.keySet()) {
            if (!word2DocIdFreqListMap.containsKey(word)) {
                word2DocIdFreqListMap.put(word, new StringBuilder());
            }
            word2DocIdFreqListMap.get(word).append(docId).append(":").append(word2FreqMap.get(word)).append("#");
        }
    }

    public void writeMapToFile(int fileId) {
        List<String> res = new ArrayList<>();
        for (String word : word2DocIdFreqListMap.keySet()) {
            res.add(word + "-" + word2DocIdFreqListMap.get(word) + "\n");
        }
        word2DocIdFreqListMap.clear();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileId + FileUtils.PARTITION_FILE_PATH));
            for (String temp : res) {
                out.write(temp);
            }
            out.close();
        } catch (IOException e) {
            System.out.println("IOE");
        }

    }

    public void closeWriterAndOutputStream() throws IOException {
        pageTableWriter.write(docId + " " + allDocLength);
        docTextOutputStream.close();
        pageTableWriter.close();
    }
}
