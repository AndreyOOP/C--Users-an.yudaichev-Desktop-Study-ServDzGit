package Testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChunkEncoder {

    private byte[] chunkedBytes;
    private int chunkSize;

    public ChunkEncoder(){
        chunkSize = 30;
    }

    public ChunkEncoder(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int write(byte[] buf){

        if(buf == null)
            return -1;

        try {

            chunkedBytes = toChunk(buf);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.length;
    }

    public byte[] getChunkedBytes(){
        return chunkedBytes;
    }

    private byte[] toChunk(byte[] buf) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] chunkBegin = (Integer.toHexString(chunkSize) + "\r\n").getBytes();
        byte[] chunkEnd   = "\r\n".getBytes();
        byte[] dataEnd    = "0\r\n\r\n".getBytes();
        int offset = 0;

        int qtyOfChunks = buf.length / chunkSize;
        int tail        = buf.length - qtyOfChunks*chunkSize;

        for(int i=0; i<qtyOfChunks; i++){

            writeOneBlock( chunkBegin, buf, offset, chunkSize, chunkEnd, bos);
            offset += chunkSize;
        }

        if (tail > 0) {

            chunkBegin = (Integer.toHexString(tail) + "\r\n").getBytes();
            writeOneBlock( chunkBegin, buf, offset, tail, chunkEnd, bos);
        }

        bos.write( dataEnd);

        return bos.toByteArray();
    }

    private void writeOneBlock(byte[] chunkBegin, byte[] data, int off, int chunkSize, byte[] chunkEnd, ByteArrayOutputStream bos) throws IOException {

        bos.write( chunkBegin);
        bos.write( data, off, chunkSize);
        bos.write( chunkEnd);
    }
}
