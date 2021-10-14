public class InvertedList {
    private final int postingCnt;
    private final int blockCnt;
    private final int[] docIdArray;
    private final int[] freqArray;
    private byte[] blockLastDocIdArray;
    private byte[] docIdBlockSizeArray;
    private byte[] freqBlockSizeArray;
    private byte[] docIdByteArray;
    private byte[] freqByteArray;
    private byte[] invertedListByteArray;


    public InvertedList(int postingCnt, int[] docIdArray, int[] freqArray) {
        this.postingCnt = postingCnt;
        this.blockCnt = postingCnt % 64 == 0? postingCnt / 64 : postingCnt / 64 + 1;
        this.docIdArray = docIdArray;
        this.freqArray = freqArray;
    }

    public int getPostingCnt() {
        return postingCnt;
    }

    public int getBlockCnt() {
        return blockCnt;
    }

    public int[] getDocIdArray() {
        return docIdArray;
    }

    public int[] getFreqArray() {
        return freqArray;
    }

    public byte[] getBlockLastDocIdArray() {
        return blockLastDocIdArray;
    }

    public void setBlockLastDocIdArray(byte[] blockLastDocIdArray) {
        this.blockLastDocIdArray = blockLastDocIdArray;
    }

    public byte[] getDocIdBlockSizeArray() {
        return docIdBlockSizeArray;
    }

    public void setDocIdBlockSizeArray(byte[] docIdBlockSizeArray) {
        this.docIdBlockSizeArray = docIdBlockSizeArray;
    }

    public byte[] getFreqBlockSizeArray() {
        return freqBlockSizeArray;
    }

    public void setFreqBlockSizeArray(byte[] freqBlockSizeArray) {
        this.freqBlockSizeArray = freqBlockSizeArray;
    }

    public byte[] getDocIdByteArray() {
        return docIdByteArray;
    }

    public void setDocIdByteArray(byte[] docIdByteArray) {
        this.docIdByteArray = docIdByteArray;
    }

    public byte[] getFreqByteArray() {
        return freqByteArray;
    }

    public void setFreqByteArray(byte[] freqByteArray) {
        this.freqByteArray = freqByteArray;
    }

    public byte[] getInvertedListByteArray() {
        return invertedListByteArray;
    }

    public void setInvertedListByteArray(byte[] invertedListByteArray) {
        this.invertedListByteArray = invertedListByteArray;
    }
}
