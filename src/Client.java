import Testing.ChunkEncoder;
import Testing.PostParser;

import java.lang.Exception;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Client implements Runnable {

    private Socket socketForDataExchange;
    private FileManager fileManager;
    private List<String> headers = new ArrayList<String>();

    
    public Client(Socket socketForDataExchange, String pathToServerFiles) {

        this.socketForDataExchange = socketForDataExchange;
        fileManager = new FileManager(pathToServerFiles);
    }

    public void run() {

        try {

            InputStream  inputStreamOfBrowser  = socketForDataExchange.getInputStream();
            OutputStream outputStreamToBrowser = socketForDataExchange.getOutputStream();

            try {

                do {

                    String messageOfBrowser = getDataFromBrowser( inputStreamOfBrowser);


                    ParsedMessage parsedMessage = parseMessageFromBrowser(messageOfBrowser, outputStreamToBrowser);

                    if ( parsedMessage != null) {

                        byte[] content = prepareReplyForBrowser(parsedMessage);

                        content = prepareDataForSending(content, outputStreamToBrowser);

                        outputStreamToBrowser.write(getBinaryHeaders(headers));
                        outputStreamToBrowser.write(content);
                    }

                } while ( ! Thread.currentThread().isInterrupted());

            } finally {
                socketForDataExchange.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }


    private String getDataFromBrowser(InputStream  inputStreamOfBrowser){

        try {

            String messageOfBrowser = new String( readBytesToTempBuffer( inputStreamOfBrowser));

            if( !messageOfBrowser.equalsIgnoreCase(""))
                System.out.println(messageOfBrowser);

            return messageOfBrowser;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ParsedMessage parseMessageFromBrowser(String messageOfBrowser, OutputStream outputStreamToBrowser) throws IOException {

        String method = "", url = "", version = "";

        String request = getRequestStr(messageOfBrowser, true);
        String[] parts = parseFirstLineOfRequest(request, outputStreamToBrowser);

        if( parts != null){

            method = parts[0]; url = parts[1]; version = parts[2];

            if ( !version.equalsIgnoreCase("HTTP/1.0") && !version.equalsIgnoreCase("HTTP/1.1"))
                returnStatusCode(400, outputStreamToBrowser);

            if ( !method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("POST"))
                returnStatusCode(400, outputStreamToBrowser);

            if (url.equals("/"))
                url = "/index.html";

            if ( method.equalsIgnoreCase("GET"))
                return new ParsedMessage( Request.GET, url, "");

            if( method.equalsIgnoreCase("POST")){

                String fileName = PostParser.getFileName( messageOfBrowser);
                String reqData  = PostParser.getFileData( messageOfBrowser);

                try ( ByteArrayOutputStream bos = new ByteArrayOutputStream();
                      ZipOutputStream zout = new ZipOutputStream( bos);
                ){

                    ZipEntry zipEntry = new ZipEntry( fileName);
                    zout.putNextEntry( zipEntry);

                    zout.write( reqData.getBytes());

                    return new ParsedMessage( Request.POST, new String( bos.toByteArray()), "temp.zip");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private byte[] prepareReplyForBrowser(ParsedMessage parsedMessage){

        headers.add("HTTP/1.1 200 OK\r\n");

        if( parsedMessage.getType() == Request.GET){
            return fileManager.get( parsedMessage.getData());
        }

        if( parsedMessage.getType() == Request.POST){
            headers.add("Content-disposition: attachment; filename=" + parsedMessage.getFileName() + "\r\n");
            return parsedMessage.getData().getBytes();
        }

        return null;
    }

    private byte[] prepareDataForSending(byte[] content, OutputStream outputStreamToBrowser) throws IOException {

        ProcessorsList processorsList = new ProcessorsList();
        processorsList.add(new Compressor(6));
//        processorsList.add(new Chunker(30));
        content = processorsList.process(content, headers);

        ChunkEncoder chunkEncoder = new ChunkEncoder(5);
        chunkEncoder.write(content);
        content = chunkEncoder.getChunkedBytes();



        headers.add("Transfer-Encoding: chunked\r\n");

        if (content == null)
            returnStatusCode(500, outputStreamToBrowser);

        headers.add("Content-Length: " + content.length + "\r\n"); //error appears because of this line - it ends connection after data transfer
        headers.add("Connection: close\r\n\r\n");

        return content;
    }


    private void returnStatusCode(int code, OutputStream outputStreamToBrowser) throws IOException {

        String msg = null;

        switch (code) {
            case 400:
                msg = "HTTP/1.1 400 Bad Request";
                break;
            case 404:
                msg = "HTTP/1.1 404 Not Found";
                break;
            case 500:
                msg = "HTTP/1.1 500 Internal Server Error";
                break;
        }

        byte[] resp = msg.concat("\r\n\r\n").getBytes();

        outputStreamToBrowser.write(resp);
    }

    private byte[] getBinaryHeaders(List<String> headers) {

        StringBuilder res = new StringBuilder();

        for (String s : headers)
            res.append(s);

        return res.toString().getBytes();
    }

    private byte[] readBytesToTempBuffer(InputStream in) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[ in.available()];

        if ( in.read(buf) > 0)
            baos.write(buf);

        return baos.toByteArray();
    }

    private String getRequestStr(String data, Boolean headerOnly){

        String[] s = data.split("\r\n\r\n");

        if ( headerOnly) {

            return s[0];
        } else {

            return data;
        }

    }

    private String[] parseFirstLineOfRequest(String request, OutputStream outputStreamToBrowser) throws IOException {

        if (request != null && !request.equalsIgnoreCase("")){
//            System.out.println("Request: " + request);
        }

        String[] parts = null;

        if ( !request.equalsIgnoreCase("") && request != null) {

            int idx = request.indexOf("\r\n");
            request = request.substring(0, idx);

            parts = request.split(" ");

            if (parts.length != 3) {
                returnStatusCode(400, outputStreamToBrowser);
                return null;
            }
        }

        return parts;
    }

    private byte[] postAction(String postRequest) throws IOException {

        String fileName = PostParser.getFileName( postRequest);
        System.out.println( fileName);

        String fileData = PostParser.getFileData(postRequest);
        System.out.println( fileData);

//        FileOutputStream fos = new FileOutputStream( new File(Const.PATH_TO_INDEX_HTML_ON_SERVER + "newZipFile.zip"));

        try ( ByteArrayOutputStream fos = new ByteArrayOutputStream();
              ZipOutputStream zout = new ZipOutputStream( fos)){

            if( fileData != null){

                ZipEntry zipEntry = new ZipEntry( fileName);
                zout.putNextEntry( zipEntry);

                zout.write( fileData.getBytes());
            }

            return fos.toByteArray();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "".getBytes();
    }

}