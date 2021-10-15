import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static final String PAGE_TABLE_FILE_PATH = "pageTable.txt";
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
}
