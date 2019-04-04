package common;

import messages.JiraMessageHandler;
import messages.Message;
import messages.Session;

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
 * {@code try(common.Connection connection = new common.Connection(server, socket)) {//try to read message from client}}
 *
 * client-side example:
 * {@code try(common.Connection connection = new common.Connection(client, new Socket(ip, port))) {//send messages and requests to server}}
 *
 */
public class Connection extends Message implements Closeable {
    public static final int DEFAULT_PORT = 28012;

    private Socket socket;

    /**
     * Constructor
     *
     * @param msgHandler the client object if used on client-side or server object if used on server-side.
     *                       The server-side message handling methods return false if called on client-side.
     * @param socket the socket that represents the connection between client and server.
     *               The socket will be closed when the common.Connection is closed by the close() method.
     * @throws IOException if the socket is not connected or an I/O error occurs when creating the input or output stream.
     */
    public Connection(Session session, JiraMessageHandler msgHandler, Socket socket) throws IOException {
        super(session, new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()), msgHandler);
        this.socket = socket;
    }



    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    public String getOtherIP() {
        return socket.getInetAddress().getHostAddress();
    }

    public int getOtherPort() {
        return socket.getPort();
    }

    public String getMyIP() {
        return socket.getLocalAddress().getHostAddress();
    }

    public int getMyPort() {
        return socket.getLocalPort();
    }
}
