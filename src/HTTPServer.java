
public class HTTPServer {

    private int port;
    private String pathToServerFiles;
    private ListenThread listenThread;

    public HTTPServer(int port, String pathToServerFiles) {

        this.port = port;
        this.pathToServerFiles = pathToServerFiles;
    }

    public void start() {

        listenThread = new ListenThread(port, pathToServerFiles);
        listenThread.start();
    }
    
    public void stop() {
    	listenThread.interrupt();
    }
}