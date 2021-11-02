public class DocObj {
    private final int docId;
    private final String docLink;
    private final int wordsCnt;
    private final String[] words;
    private final int[] wordsFreq;

    private double score;

    public DocObj(int docId, int cnt, String link) {
        this.docId = docId;
        this.docLink = link;
        this.wordsCnt = cnt;
        this.words = new String[wordsCnt];
        this.wordsFreq = new int[wordsCnt];
    }

    public void setScore(double score) {
        this.score = score;
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

    public String[] getWords() {
        return words;
    }

    public int[] getWordsFreq() {
        return wordsFreq;
    }
}
