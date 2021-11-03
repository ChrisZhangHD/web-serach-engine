import java.util.ArrayList;
import java.util.List;

public class DocObj {
    private final int docId;
    private final String docLink;
    private final List<String> words;
    private final List<Integer> wordsFreq;

    private double score = 0;

    public DocObj(int docId, int cnt, String link) {
        this.docId = docId;
        this.docLink = link;
        this.words = new ArrayList<>(cnt);
        this.wordsFreq = new ArrayList<>(cnt);
    }

    public DocObj(int docId, String link) {
        this(docId, 16, link);
    }

    public void addScore(double score) {
        this.score += score;
    }

    public double getScore() {
        return score;
    }

    public int getDocId() {
        return docId;
    }

    public String getDocLink() {
        return docLink;
    }

    public List<String> getWords() {
        return words;
    }

    public List<Integer> getWordsFreq() {
        return wordsFreq;
    }
}
