import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DocHandle {

    private final PostingsHandle postingsHandle;

    public DocHandle() throws IOException {
        postingsHandle = new PostingsHandle();
    }

    public void readFile(String filePath, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        boolean isDoc = false;
        StringBuilder docBuffer = new StringBuilder();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            int i = 0;
            while (inputStream.read(buffer) != -1) {
                String s = new String(buffer, StandardCharsets.UTF_8);
                String[] lines = s.split("\n");
                for (String line : lines) {
                    if (line.equals("<DOC>")) {
                        isDoc = true;
                    }
                    if (isDoc) {
                        docBuffer.append(line).append(" ");
                    }
                    if (line.equals("</DOC>")) {
                        docBuffer.append(line);
                        String docText = parseDoc(docBuffer.toString());
                        postingsHandle.buildDocPosingIndex(docText);
                        docBuffer = new StringBuilder();
                        isDoc = false;
                    }
                }
                postingsHandle.writeMapToFile(i);
                i += 1;
            }
            postingsHandle.closePageTableWriter();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String parseDoc(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("TEXT").text();
    }
}
