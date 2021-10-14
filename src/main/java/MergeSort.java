import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MergeSort {

    private String[] fileNameArray = null;
    private final PriorityQueue<PartitionFile> pq = new PriorityQueue<>(new Comparator<PartitionFile>() {
        @Override
        public int compare(PartitionFile partitionFile1, PartitionFile partitionFile2) {
            if (partitionFile1.getCurWord().compareTo(partitionFile2.getCurWord()) > 0) {
                return 1;
            } else if (partitionFile1.getCurWord().compareTo(partitionFile2.getCurWord()) == 0) {
                return Integer.compare(partitionFile1.getFileNo(), partitionFile2.getFileNo());
            } else {
                return -1;
            }
        }
    });

    public void sortSplitFile() {
        try {
            Process process = Runtime.getRuntime().exec("sh unixsort.sh");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            fileNameArray = line.split(" ");
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergePartitionFile() {
        String prevWord = null;
        StringBuilder prevDocIdFreq = new StringBuilder();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(FileUtils.MERGE_SORT_FILE_PATH));
            while (pq.size() > 0) {
                PartitionFile partitionFile = pq.poll();
                String curWord = partitionFile.getCurWord();
                String curDocIdFreq = partitionFile.getDocIdFreq();
                if (prevWord == null) {
                    prevWord = curWord;
                } else {
                    if (!curWord.equals(prevWord)) {
                        String newIndex = prevWord + "-" + prevDocIdFreq.toString() + "\n";
                        System.out.println(prevWord);
                        out.write(newIndex);
                        prevWord = curWord;
                        prevDocIdFreq.setLength(0);
                    }
                }
                prevDocIdFreq.append(curDocIdFreq);
                if (partitionFile.update()) {
                    pq.add(partitionFile);
                } else {
                    partitionFile.close();
                }
            }
            String newIndex = prevWord + "-" + prevDocIdFreq.toString() + "\n";
            out.write(newIndex);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void putPartitionFileToPq() {
        for (String partitionFileName : fileNameArray) {
            try {
                PartitionFile partitionFile = new PartitionFile(partitionFileName);
                if (partitionFile.update()) {
                    pq.add(partitionFile);
                }
            } catch (FileNotFoundException e) {
                System.out.println("No such File found.");
            } catch (IOException e) {
                System.out.println("IOE");
            }
        }
    }

    public String[] getFileNameArray() {
        return fileNameArray;
    }

    public static void main(String[] args) {
        MergeSort mergeSort = new MergeSort();
        mergeSort.sortSplitFile();
        String[] fileNameArray = mergeSort.getFileNameArray();
        System.out.println(fileNameArray.length);
        mergeSort.putPartitionFileToPq();
        mergeSort.mergePartitionFile();
    }

}
