import Client.Client;
import Server.Server;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A helper class for implementing the minijira-protocol on server-side and client-side
 *
 * The overrided message handling methods return false if called on the wrong side, otherwise
 * call the real message handling methods in Client or Server classes.
 *
 * Server-side example:
 * {@code try(Connection connection = new Connection(server, socket)) {//try to read message from client}}
 *
 * Client-side example:
 * {@code try(Connection connection = new Connection(client, new Socket(ip, port))) {//send messages and requests to server}}
 *
 */
public class Connection extends Message implements Closeable {
    private Socket socket;
    private Server server = null; // we would not need these references back to the server and client if Message.readMessage()
    private Client client = null; // would return the message type and message content as byte[] or Object.
                                  // The message handling code could be written in Client and Server separately.

    /**
     * Constructor
     *
     * @param clientOrServer the client object if used on client-side or server object if used on server-side.
     *                       The server-side message handling methods return false if called on client-side.
     * @param socket the socket that represents the connection between client and server.
     *               The socket will be closed when the Connection is closed by the close() method.
     * @throws IOException if the socket is not connected or an I/O error occurs when creating the input or output stream.
     */
    public Connection(Object clientOrServer, Socket socket) throws IOException {
        super(new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()));
        this.socket = socket;
        if (clientOrServer instanceof Server) {
            server = (Server)clientOrServer;
        }
        else if (clientOrServer instanceof Client) {
            client = (Client)clientOrServer;
        }
        else {
            throw new RuntimeException("Connection requires an instance of Client or Server to call message handling methods");
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public boolean createTask(byte[] data) {
        if (server != null) {
            return server.createTask(data);
        }
        return false;
    }

    @Override
    public boolean removeTask(byte[] data) {
        if (server != null) {
            return server.removeTask(data);
        }
        return false;
    }

    @Override
    public boolean updateTimeTask(byte[] data) {
        if (server != null) {
            return server.updateTimeTask(data);
        }
        return false;
    }

    @Override
    public boolean setStatusTask(byte[] data) {
        if (server != null) {
            return server.setStatusTask(data);
        }
        return false;
    }

    public void close() throws IOException {
        socket.close();
    }
}
