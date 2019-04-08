package server;

import auth.SecurityHelper;
import common.Connection;
import common.Project;
import common.User;
import data.*;
import messages.JiraMessageHandler;
import messages.MessageType;
import messages.Session;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

public class SessionForClient implements Runnable, JiraMessageHandler {
    private Connection connection;
    private Session session;
    private Server server;

    public SessionForClient(Server server, Socket socket) throws IOException {
        this.server = server;
        this.connection = new Connection(null, this, socket);
    }

    long getSessionID() {
        return session.getSessionId();
    }

    @Override
    public void run() {
        while (!connection.isClosed()) {
            try {
                connection.readMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void runSingleRequest() {
        try {
            // login / session
            if (!connection.isClosed())
                connection.readMessage();
            // request
            if (!connection.isClosed())
                connection.readMessage();
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public RawError createTask(RawTask message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError removeTask(Long message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError updateTask(RawTask message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError getServerTaskList(Object message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError setSession(RawSession session) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError login(RawLogin log) {
        for (User user : server.getUsers()) {
            //find user
            if (user.getUsername().equalsIgnoreCase(log.username) || user.getUserEmail().equalsIgnoreCase(log.username)) {
                //check password
                try {
                    if (server.getUserSalt(user) == null)
                        return new RawError("User password salt missing.");
                    else if (Arrays.equals(user.getPasswordHash(), SecurityHelper.passwordToHash(log.password, server.getUserSalt(user)))) {
                        // create session
                        session = new Session(SecurityHelper.generateSessionKey(), server.getNewValidSessID(), user.getUserId(),
                                connection.getOtherIP(), connection.getOtherPort(), connection.getMyIP(), connection.getMyPort());
                        // send session information to client
                        connection.sendMessage(session, MessageType.SETSESSION);
                        // return no error
                        return null;
                    } else
                        return new RawError("Wrong password or username.");
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new RawError("Wrong password or username.");
    }

    @Override
    public RawError getProjectList() {
        if (session == null || !session.isValid())
            return new RawError("You must be logged in to see your projects.");

        for (User user : server.getUsers()) {
            if (user.getUserId() == session.getUserId()) {
                List<Project> projects = user.getProjects();

                try {
                    if (projects == null) {
                        // send empty project name list
                        connection.sendMessage(new RawProjectNameList(null, null), MessageType.SETPROJECTLIST);
                        return null;
                    } else {
                        // send project name list
                        connection.sendMessage(Project.getProjectNameList(projects), MessageType.SETPROJECTLIST);
                        return null;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new RawError("User is missing from the database.");
    }

    @Override
    public RawError setProjectList(RawProjectNameList message) {
        return new RawError("Invalid request on server side");
    }

    @Override
    public RawError getProject(Long message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError setProject(RawProject message) {
        return new RawError("Invalid request on server side");
    }
}
