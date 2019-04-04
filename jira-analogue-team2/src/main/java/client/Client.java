package client;

import apacheUI.TUI;
import common.Connection;
import common.Project;
import data.RawLogin;
import data.RawProject;
import data.RawProjectNameList;
import messages.JiraMessageHandler;
import messages.MessageType;
import messages.Session;
import messages.messagetypes.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;

public class Client implements JiraMessageHandler {
    private TUI tui;
    private Connection connection;
    private Session clientSession;
    private String username;
    private RawProjectNameList projectNameList;
    private Project openedProject;

    Client() {
        tui = new TUI(this);
    }

    void startTUI(String[] args) throws IOException, InterruptedException {
        updateProjects();
        tui.startTerminal(args);
    }

    boolean updateProjects() throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(new GetProjectListMessage());
            return connection.readMessage() == MessageType.SETPROJECTLIST;
        }
        return false;


    }

    boolean isConnected() {
        return connection != null && !connection.isClosed();
    }

    private void connect(String serverIP, int port, Session session) throws IOException, InterruptedException {
        System.out.print("Connecting..");
        Connection old = connection;
        while (connection == old) {
            try {
                Socket socket = new Socket(serverIP, port);
                connection = new Connection(session, this, socket);
                System.out.println("\nConnected.");
                break;
            } catch (ConnectException e) {
                Thread.sleep(1000);
                System.out.print('.');
            }
        }
    }

    void connect(String serverIP, int port) throws InterruptedException, IOException {
        connect(serverIP, port, null);
    }

    void connect(Session session) throws InterruptedException, IOException {
        connect(session.getServerIP(), session.getClientPort(), session);
    }

    /**
     * Makes sure the client is connected to the server. If client is not connected, tries to connect with the session information
     * @return true if connection is still open or managed to reconnect. False if failed to reconnect or client is not logged in.
     * @throws IOException
     * @throws InterruptedException
     */
    boolean reconnect() throws IOException, InterruptedException {
        if (isConnected() || clientSession != null) {
            while(!isConnected()) {
                connect(clientSession);
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Request the selected project from server.
     * @param index index of the project in arrays projectNameList.projectNames and .projectIds.
     * @return true if the client received the project, false otherwise.
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean selectProject(int index) throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(new GetProjectMessage(projectNameList.projectIds[index]));
            return connection.readMessage() == MessageType.SETPROJECT;
        }
        return false;
    }

    boolean login(String username, String password) throws IOException {
        this.username = username;
        if (isConnected()) {
            System.out.println("Logging in.");
            connection.sendMessage(new LoginMessage(new RawLogin(username, password)));
            return connection.readMessage() == MessageType.SETSESSION;
        } else return false;
    }


    @Override
    public ErrorMessage createTask(CreateTaskMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage removeTask(RemoveTaskMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage updateTask(UpdateTaskMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage getServerTaskList(GetServerTaskListMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage setSession(SetSessionMessage message) {
        clientSession = (Session) message.getData();
        if (clientSession != null)
            tui.setDisplayUsername(username);
        else
            tui.setDisplayUsername("");
        return null;
    }

    @Override
    public ErrorMessage login(LoginMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage getProjectList(GetProjectListMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage setProjectList(SetProjectListMessage message) {
        projectNameList = (RawProjectNameList)message.getData();
        tui.setProjects(Arrays.asList(projectNameList.projectNames));
        return null;
    }

    @Override
    public ErrorMessage getProject(GetProjectMessage message) {
        return new ErrorMessage("Invalid request on client side");
    }

    @Override
    public ErrorMessage setProject(SetProjectMessage message) {
        openedProject = new Project((RawProject) message.getData());
        return null;
    }
}
