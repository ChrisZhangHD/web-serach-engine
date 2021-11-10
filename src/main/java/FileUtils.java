import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {

    public static final String PAGE_TABLE_FILE_PATH = "pageTable.txt";
    public static final String DOC_TEXT_FILE = "docText.bin";
    public static final String INVERTED_LIST_BIN_FILE_PATH = "invertedList.bin";
    public static final String LEXICON_FILE_PATH = "lexicon.txt";
    public static final String PARTITION_FILE_PATH = "part.txt";
    public static final String MERGE_SORT_FILE_PATH = "mergesort.txt";
    public static final String DATASET_FILE_PATH = "msmarco-docs.trec";

    public static void createFile(String filePath) {
        try {
            File file = new File(filePath);
            file.createNewFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static int getDocCnt() {
        try {
            Process process = Runtime.getRuntime().exec("wc -l " + PAGE_TABLE_FILE_PATH);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = input.readLine().trim();
            return Integer.parseInt(result.split(" ")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getLineFromFile(String filePath, int line) {
        try {
            String command = "sed -n " + line + "p " + filePath;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
