package server;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("server.");

        Server server = new Server();
        server.loadData();

        // run server with multiple threads (connections stay open until client disconnects)
        server.run();
        // run server on single thread (server disconnects after receiving 2 messages: login/setsession and a request message)
        //server.runSingleThread();
    }
}
