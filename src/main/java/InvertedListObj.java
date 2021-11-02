public class InvertedListObj {

    private final static int BLOCK_SIZE = 64;

    private final byte[] buffer;
    private final int postingCnt;
    private final int blockCnt;
    private final String word;
    private final int[] docIdBlockSizeArray;
    private final int[] freqBlockSizeArray;
    private final int[] lastDocIdBlockArray;
    private int index;
    private int docIdStartIndex;
    private int freqStartIndex;

    public InvertedListObj(byte[] buffer, String word) {
        this.index = 0;
        this.buffer = buffer;
        this.word = word;
        postingCnt = varByteDecode();
        blockCnt = postingCnt % BLOCK_SIZE == 0 ? postingCnt / 64 : postingCnt / 64 + 1;
        docIdBlockSizeArray = new int[blockCnt];
        freqBlockSizeArray = new int[blockCnt];
        lastDocIdBlockArray = new int[blockCnt];
        setMetadata();
    }

    private void setMetadata() {
        int allDocIdBlockSize = 0;
        for (int i = 0; i < blockCnt; i++) {
            int num = varByteDecode();
            docIdBlockSizeArray[i] = num;
            allDocIdBlockSize += num;
        }
        for (int i = 0; i < blockCnt; i++) {
            int num = varByteDecode();
            freqBlockSizeArray[i] = num;
        }
        for (int i = 0; i < blockCnt; i++) {
            int num = varByteDecode();
            lastDocIdBlockArray[i] = num;
        }
        docIdStartIndex = index;
        freqStartIndex = index + allDocIdBlockSize;

    }

    public int getPostingCnt() {
        return postingCnt;
    }

    public String getWord() {
        return word;
    }

    public int[] getDocIdBlockSizeArray() {
        return docIdBlockSizeArray;
    }

    public int[] getFreqBlockSizeArray() {
        return freqBlockSizeArray;
    }

    public int[] getLastDocIdBlockArray() {
        return lastDocIdBlockArray;
    }

    public int[] getDocIdBlockArray(int start, int block) {
        index = start;
        int length = block == blockCnt - 1 ? postingCnt - 64 * block : 64;
        int[] docIdBlockArray = new int[length];
        for (int i = 0; i < length; i++) {
            docIdBlockArray[i] = varByteDecode();
        }
        return docIdBlockArray;
    }

    public int[] getFreqBlockArray(int start, int block){
        index = start;
        int length = block == blockCnt - 1 ? postingCnt - 64 * block : 64;
        int[] freqBlockArray = new int[length];
        for (int i = 0; i < length; i++) {
            freqBlockArray[i] = varByteDecode();
        }
        return freqBlockArray;
    }

    public int varByteDecode(){
        int val = 0, shift = 0;
        int b = buffer[index] & 255;
        while (b < 128) {
            val = val + (b << shift);
            shift = shift + 7;
            index += 1;
            b = buffer[index] & 255;
        }
        val = val + ((b - 128) << shift);
        index += 1;
        return (val);
    }

    public int getDocIdStartIndex() {
        return docIdStartIndex;
    }

    public int getFreqStartIndex() {
        return freqStartIndex;
    }
}
