import java.lang.Thread;

public class Main {

    public static void main(String[] args) {

        final HTTPServer server = new HTTPServer( Const.SERVER_PORT, Const.PATH_TO_INDEX_HTML_ON_SERVER);

        server.start();

        System.out.println("Server started...");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.stop();
                System.out.println("Server stopped!");
            }
        });
    }
}
