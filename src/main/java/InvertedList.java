public class InvertedList {
    private final int postingCnt;
    private final int blockCnt;
    private final int[] blockLastDocIdArray;
    private final int[] docIdBlockSizeArray;
    private final int[] freqBlockSizeArray;
    private final int[] docIdArray;
    private final int[] freqArray;

    public InvertedList(int postingCnt, int[] docIdArray, int[] freqArray) {
        this.postingCnt = postingCnt;
        this.blockCnt = postingCnt % 64 == 0? postingCnt / 64 : postingCnt / 64 + 1;
        this.blockLastDocIdArray = new int[blockCnt];
        this.docIdBlockSizeArray = new int[blockCnt];
        this.freqBlockSizeArray = new int[blockCnt];
        this.docIdArray = docIdArray;
        this.freqArray = freqArray;
    }

    public int getPostingCnt() {
        return postingCnt;
    }

    public int getBlockCnt() {
        return blockCnt;
    }

    public int[] getBlockLastDocIdArray() {
        return blockLastDocIdArray;
    }

    public int[] getDocIdBlockSizeArray() {
        return docIdBlockSizeArray;
    }

    public int[] getFreqBlockSizeArray() {
        return freqBlockSizeArray;
    }

    public int[] getDocIdArray() {
        return docIdArray;
    }

    public int[] getFreqArray() {
        return freqArray;
    }


}
