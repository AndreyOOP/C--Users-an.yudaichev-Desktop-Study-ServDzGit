package Testing;

public class MainTest {

    public static void main(String[] args) {

        ChunkEncoder chunkEncoder = new ChunkEncoder(5);
        ChunkDecoder chunkDecoder = new ChunkDecoder();

        byte[] tst = "1234512345123451234".getBytes();
        chunkEncoder.write( tst);
        tst = chunkEncoder.getChunkedBytes();

        System.out.println("Encoded");
        System.out.println( new String( tst));

        chunkDecoder.read(tst);
        tst = chunkDecoder.getDecodedBytes();

        System.out.println("Decoded");
        System.out.println( new String( tst));

    }
}
