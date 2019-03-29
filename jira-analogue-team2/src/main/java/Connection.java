import messages.JiraMessageHandler;
import messages.Message;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A helper class for implementing the minijira-protocol on server-side and client-side
 *
 * The overrided message handling methods return false if called on the wrong side, otherwise
 * call the real message handling methods in client or server classes.
 *
 * server-side example:
 * {@code try(Connection connection = new Connection(server, socket)) {//try to read message from client}}
 *
 * client-side example:
 * {@code try(Connection connection = new Connection(client, new Socket(ip, port))) {//send messages and requests to server}}
 *
 */
public class Connection extends Message implements Closeable {
    private Socket socket;

    /**
     * Constructor
     *
     * @param msgHandler the client object if used on client-side or server object if used on server-side.
     *                       The server-side message handling methods return false if called on client-side.
     * @param socket the socket that represents the connection between client and server.
     *               The socket will be closed when the Connection is closed by the close() method.
     * @throws IOException if the socket is not connected or an I/O error occurs when creating the input or output stream.
     */
    public Connection(JiraMessageHandler msgHandler, Socket socket) throws IOException {
        super(new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()), msgHandler);
        this.socket = socket;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }
}
