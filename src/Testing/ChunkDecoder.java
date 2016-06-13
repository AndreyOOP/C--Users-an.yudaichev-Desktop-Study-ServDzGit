package Testing;

import java.io.ByteArrayOutputStream;

public class ChunkDecoder {

    private byte[] buffer;
    private int off = 0;

    public ChunkDecoder(){}

    public int read(byte buf[]){

        if(buf == null)
            return -1;

        buffer = toDecode(buf);

        return buf.length;
    }

    private byte[] toDecode(byte[] buf){

        ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();
        int chunkSize = -1;

        while ( chunkSize != 0){

            chunkSize = getChunkSize(buf, off);

            decodedStream.write(buf, off+2, chunkSize); // 2 is length of \r\n

            off += chunkSize + 4; // length of \r\n + chunk data + \r\n
        }

        return decodedStream.toByteArray();
    }

    private int getChunkSize(byte[] buf, int off){

        for(int i=0; i<(buf.length-off-1); i++){

            if( isNextLine( buf[off+i], buf[off+i+1])){

                byte[] chunkSizeInBytes = new byte[i];
                System.arraycopy(buf, off, chunkSizeInBytes, 0, i);
                this.off += i;

                return Integer.parseInt( new String(chunkSizeInBytes), 16);
            }
        }

        return -1;
    }

    private Boolean isNextLine(byte a, byte b){ //  ==\r\n
        return a==13 && b==10;
    }

    public byte[] getDecodedBytes() {
        return buffer;
    }
}
