import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ListenThread extends Thread {

    private int port;
    private String pathToServerFiles;

    public ListenThread(int port, String pathToServerFiles) {
        this.port = port;
        this.pathToServerFiles = pathToServerFiles;
    }

    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            ExecutorService pool = Executors.newFixedThreadPool( Const.POOL_QTY);

            try {

                while ( !isInterrupted()){

                    onServiceSocketAccept( serverSocket, pool);
                }

            } finally {
                serverSocket.close();
                pool.shutdown();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private void onServiceSocketAccept(ServerSocket serverSocket, ExecutorService pool) throws IOException {

        Socket socket = serverSocket.accept();

        Client client = new Client(socket, pathToServerFiles);

        try {

            pool.submit(client);

        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            System.out.println("Cannot schedule for execution");
        }

        try {
            Thread.sleep( Const.SERVER_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
