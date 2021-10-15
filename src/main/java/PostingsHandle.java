import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    public PostingsHandle() throws IOException {
        this.pageTableWriter = new BufferedWriter(new FileWriter(FileUtils.PAGE_TABLE_FILE_PATH));
    }

    public void buildDocPosingIndex(int docId, String docText) throws IOException {
        String[] docTextArray = docText.split(" ");
        Map<String, Integer> word2FreqMap = new HashMap<>();
        String curWord;
        String website = docTextArray[0];
        pageTableWriter.write(docId + " " + website + "\n");
        for(int i = 1; i < docTextArray.length; i ++) {
            curWord = docTextArray[i];
            // if the length of word more than 20. Omit it.
            if (curWord.length() > 20){
                continue;
            }
            Matcher isValid = p.matcher(curWord);
            if(isValid.matches()) {
                curWord = curWord.toLowerCase();
                word2FreqMap.put(curWord, word2FreqMap.getOrDefault(curWord, 0) + 1);
            }
        }
        for(String word: word2FreqMap.keySet()){
            if(!word2DocIdFreqListMap.containsKey(word)){
                word2DocIdFreqListMap.put(word, new StringBuilder());
            }
            word2DocIdFreqListMap.get(word).append(docId).append(":").append(word2FreqMap.get(word)).append("#");
        }
    }

    public void writeMapToFile(int fileId){
        List<String> res = new ArrayList<>();
        for(String word: word2DocIdFreqListMap.keySet()) {
            res.add(word + "-" + word2DocIdFreqListMap.get(word) + "\n");
        }
        word2DocIdFreqListMap.clear();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileId + FileUtils.PARTITION_FILE_PATH));
            for(String temp: res) {
                out.write(temp);
            }
            out.close();
        } catch (IOException e) {
            System.out.println("IOE");
        }

    }

    public void closePageTableWriter() throws IOException {
        pageTableWriter.close();
    }
}
