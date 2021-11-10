import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            FileUtils.createFile(FileUtils.PAGE_TABLE_FILE_PATH);
            FileUtils.createFile(FileUtils.DOC_TEXT_FILE);
            DocHandle docHandle = new DocHandle();
            int bufferSize = 1024 * 1024 * 128;
            String filePath = FileUtils.DATASET_FILE_PATH;
            docHandle.readFile(filePath, bufferSize);
            System.out.println("split success");

            MergeSort mergeSort = new MergeSort();
            mergeSort.sortSplitFile();
            mergeSort.putPartitionFileToPq();
            mergeSort.mergePartitionFile();
            System.out.println("merge success");

            FileUtils.createFile(FileUtils.LEXICON_FILE_PATH);
            FileUtils.createFile(FileUtils.INVERTED_LIST_BIN_FILE_PATH);

            InvertedIndex invertedIndex = new InvertedIndex(bufferSize);
            invertedIndex.createBinFile();
            invertedIndex.readFileByLine(FileUtils.MERGE_SORT_FILE_PATH);
            System.out.println("build success");
            long endTime = System.currentTimeMillis();
            System.out.println("Run Time： " + (endTime - startTime) / 60000 + "m");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
