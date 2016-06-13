import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private String path;
    private static ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<>();

    public FileManager(String pathToServerFiles) {

        this.path = removeLastSlash( pathToServerFiles);
    }

    public byte[] get(String url) {

        byte[] serverData = map.get(url);

        if (serverData != null)
            return serverData;

        serverData = readDataFromFileOnServer( url);

        map.put( url, serverData);
        System.out.println("Data added to cache *******************");

        return serverData;
    }

    public void clearCache(){

        if( !map.isEmpty())
            map.clear();
    }


    private String removeLastSlash(String url){

        if( url.endsWith("\\") || url.endsWith("/"))
            return url.substring(0, url.length()-1);

        return url;
    }

    public byte[] readDataFromFileOnServer(String url){

        String fullPath = path.replace('\\', '/') + url;

        try( RandomAccessFile file = new RandomAccessFile( fullPath, "r")) {

            byte[] serverData = new byte[(int) file.length()];

            file.read(serverData, 0, serverData.length);

            return serverData;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
