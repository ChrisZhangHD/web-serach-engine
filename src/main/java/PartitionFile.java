import java.io.*;

public class PartitionFile {

    private final String fileName;
    private final FileInputStream inputStream;
    private final BufferedReader bufferedReader;
    private final int fileNo;
    private String curWord;
    private String docIdFreq;

    public PartitionFile(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        this.fileNo = Integer.parseInt(fileName.split("\\.")[0].substring(4));
        this.inputStream = new FileInputStream(fileName);
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public boolean update() throws IOException {
        String newLine = bufferedReader.readLine();
        if (newLine == null) {
            return false;
        }
        String[] tempArray = newLine.split("-");
        curWord = tempArray[0];
        docIdFreq = tempArray[1];
        return true;
    }

    public String getLineFromFile() throws IOException {
        return bufferedReader.readLine();
    }

    public void close() throws IOException {
        inputStream.close();
        bufferedReader.close();
    }

    public int getFileNo() {
        return fileNo;
    }

    public String getCurWord() {
        return curWord;
    }

    public String getDocIdFreq() {
        return docIdFreq;
    }
}
