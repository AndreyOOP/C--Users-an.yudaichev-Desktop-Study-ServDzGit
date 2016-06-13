package Testing;

public class PostParser {

    public static String getFileName(String data){

        int nameStart, nameEnd;

        if( (nameStart = data.indexOf("filename=\"")) > 0){

            nameStart += 10;

            nameEnd = data.substring(nameStart).indexOf("\"");

            return data.substring(nameStart, nameStart + nameEnd);
        }

        return null;
    }

    public static String getFileData(String data){

        int beginOfFile = data.indexOf("----");

        if ( beginOfFile > 0) {

            data = data.substring(beginOfFile);
            beginOfFile = data.indexOf("Content-Type");

            if ( beginOfFile > 0) {

                data = data.substring(beginOfFile);

                beginOfFile = data.indexOf("\r\n");

                int endOfFile = data.indexOf("----");

                String fileData = data.substring(beginOfFile, endOfFile);

                return fileData.substring(4, fileData.length()-2);
            }
        }

        return null;
    }
}
