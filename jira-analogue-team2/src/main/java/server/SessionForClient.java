package server;

import auth.SecurityHelper;
import common.Connection;
import common.Project;
import common.User;
import data.RawLogin;
import data.RawProjectNameList;
import messages.JiraMessageHandler;
import messages.Session;
import messages.messagetypes.*;

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
        return session.getSessionid();
    }

    @Override
    public void run() {
        while(!connection.isClosed()) {
            try {
                connection.readMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public ErrorMessage createTask(CreateTaskMessage message) {
        return new ErrorMessage("Not yet implemented :(");
    }

    @Override
    public ErrorMessage removeTask(RemoveTaskMessage message) {
        return new ErrorMessage("Not yet implemented :(");
    }

    @Override
    public ErrorMessage updateTask(UpdateTaskMessage message) {
        return new ErrorMessage("Not yet implemented :(");
    }

    @Override
    public ErrorMessage getServerTaskList(GetServerTaskListMessage message) {
        return new ErrorMessage("Not yet implemented :(");
    }

    @Override
    public ErrorMessage setSession(SetSessionMessage session) {
        return new ErrorMessage("Invalid request on server side");
    }

    @Override
    public ErrorMessage login(LoginMessage message) {
        RawLogin log = (RawLogin) message.getData();
        for (User user : server.getUsers()) {
            //find user
            if (user.getUsername().equalsIgnoreCase(log.username) || user.getUserEmail().equalsIgnoreCase(log.username)) {
                //check password
                try {
                    if (server.getUserSalt(user) == null)
                        return new ErrorMessage("User password salt missing.");
                    else if (Arrays.equals(user.getPasswordHash(), SecurityHelper.passwordToHash(log.password, server.getUserSalt(user)))) {
                        // create session
                        session = new Session(SecurityHelper.generateSessionKey(), server.getNewValidSessID(), user.getUserId(),
                                connection.getOtherIP(), connection.getOtherPort(), connection.getMyIP(), connection.getMyPort());
                        // send session information to client
                        connection.sendMessage(new SetSessionMessage(session));
                        // return no error
                        return null;
                    } else
                        return new ErrorMessage("Wrong password or username.");
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ErrorMessage("Wrong password or username.");
    }

    @Override
    public ErrorMessage getProjectList(GetProjectListMessage message) {
        if (session == null || !session.isValid())
            return new ErrorMessage("You must be logged in to see your projects.");

        for (User user : server.getUsers()) {
            if (user.getUserId() == session.getUserID()) {
                List<Project> projects = user.getProjects();

                try {
                    if (projects == null) {
                        // send empty project name list
                        connection.sendMessage(new SetProjectListMessage(new RawProjectNameList(null, null)));
                        return null;
                    }
                    else {
                        // send project name list
                        connection.sendMessage(new SetProjectListMessage(Project.getProjectNameList(projects)));
                        return null;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ErrorMessage("User is missing from the database.");
    }

    @Override
    public ErrorMessage setProjectList(SetProjectListMessage message) {
        return new ErrorMessage("Invalid request on server side");
    }

    @Override
    public ErrorMessage getProject(GetProjectMessage message) {
        return new ErrorMessage("Not yet implemented :(");
    }

    @Override
    public ErrorMessage setProject(SetProjectMessage message) {
        return new ErrorMessage("Invalid request on server side");
    }
}
