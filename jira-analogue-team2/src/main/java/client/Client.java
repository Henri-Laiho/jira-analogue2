package client;

import common.Connection;
import common.Project;
import common.Task;
import common.User;
import data.*;
import lanternaUI.TUI;
import messages.JiraMessageHandler;
import messages.MessageType;
import messages.Session;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class Client implements JiraMessageHandler {
    private static final int RESPONSE_TIMEOUT_MS = 1000;

    private TUI tui;
    private Connection connection;
    private Session clientSession;
    private String username;
    private RawProjectNameList projectNameList;
    private ClientProject openedProject;
    private User user;

    Client() {
        tui = new TUI(this);
    }

    /**
     * Request the list of projects for this user from the server;
     * (wait for response containing the list;)
     * and start terminal window.
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    void startTUI(String[] args) throws IOException, InterruptedException {
        updateProjects();
        tui.startTerminal(args);
    }

    void startProjectTUI(String[] args) throws IOException, InterruptedException {
        updateProjects();
        tui.setEditProject(true);
        tui.startTerminal(args);


    }

    public long getUserId() {
        return user == null ? -1 : user.getUserId();
    }

    public int getUserRightsInProject() {
        if (user != null)
            return user.getRightsInProject(openedProject);
        else
            return RawProjectRights.WANNABE.getRightsValue();
        //TODO: remove comments and make the user rights system work:
        //throw new RuntimeException("getUserRightsInProject when user is null");
    }

    public Project getOpenedProject() {
        return openedProject;
    }

    private MessageType waitForError() throws IOException, InterruptedException {
        return waitForError(RESPONSE_TIMEOUT_MS);
    }

    private MessageType waitForError(int timeout_ms) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<MessageType> messageTypeFuture = executor.submit(() -> connection.readMessage());
        MessageType messageType = null;
        try {
            messageType = messageTypeFuture.get(timeout_ms, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            messageTypeFuture.cancel(true);
            System.out.println("Didn't receive response from server. Reconnecting...");
            connection.close();
            reconnect();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException)
                throw new IOException(e);
            else
                throw new RuntimeException(e);
        }
        executor.shutdownNow();
        return messageType;
    }

    public boolean sendUpdateProject() throws IOException, InterruptedException {
        if (openedProject != null && reconnect()) {
            connection.sendMessage(openedProject.toRawProject(), MessageType.UPDATEPROJECT);
            //return connection.readMessage() == MessageType.RESPONSE;
            return waitForError() == MessageType.RESPONSE;
        }
        return false;
    }

    public boolean sendUpdateTask(Task task) throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(task.toRawTask(), MessageType.UPDATETASK);
            //return connection.readMessage() == MessageType.RESPONSE;
            return waitForError() == MessageType.RESPONSE;
        }
        return false;
    }

    public boolean sendCreateTask(Task task) throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(task.toRawTask(), MessageType.CREATETASK);
            //return connection.readMessage() == MessageType.RESPONSE;
            return waitForError() == MessageType.RESPONSE;
        }
        return false;
    }

    public boolean sendGetTask(long taskId) throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(taskId, MessageType.GETTASK);
            //return connection.readMessage() == MessageType.RESPONSE;
            return waitForError() == MessageType.SETTASK;
        }
        return false;
    }

    /**
     * Request the list of projects for this user from the server;
     * and wait for response containing the list.
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean updateProjects() throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(null, MessageType.GETPROJECTLIST);
            //return connection.readMessage() == MessageType.SETPROJECTLIST;
            return waitForError(5000) == MessageType.SETPROJECTLIST;
        }
        return false;
    }

    /**
     * @return true if the connection is not closed and not null, false otherwise.
     */
    boolean isConnected() {
        return connection != null && !connection.isClosed();
    }

    /**
     * Tries to connect to the server until the server accepts the connection.
     *
     * @param serverIP server IP.
     * @param port     server Port.
     * @param session  the session from previous login or null if not yet logged in.
     * @throws IOException
     * @throws InterruptedException
     */
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
     *
     * @return true if connection is still open or managed to reconnect. False if failed to reconnect or client is not logged in.
     * @throws IOException
     * @throws InterruptedException
     */
    boolean reconnect() throws IOException, InterruptedException {
        if (isConnected() || clientSession != null) {
            while (!isConnected()) {
                if (!clientSession.isValid())
                    return false;
                connect(clientSession);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Request the selected project from server.
     *
     * @param index index of the project in arrays projectNameList.projectNames and .projectIds.
     * @return true if the client received the project, false otherwise.
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean selectProject(int index) throws IOException, InterruptedException {
        if (reconnect()) {
            connection.sendMessage(projectNameList.projectIds[index], MessageType.GETPROJECT);
            //return connection.readMessage() == MessageType.SETPROJECT;
            return waitForError(5000) == MessageType.SETPROJECT;
        }
        return false;
    }

    /**
     * Try to log in to the server
     *
     * @param username username or email.
     * @param password password.
     * @return true if logged in succesfully, false otherwise.
     * @throws IOException
     */
    boolean login(String username, String password) throws IOException {
        this.username = username;
        if (isConnected()) {
            System.out.println("Logging in.");
            connection.sendMessage(new RawLogin(username, password), MessageType.LOGIN);
            return connection.readMessage() == MessageType.SETSESSION;
        } else return false;
    }

    // See the JiraMessageHandler interface for more details on the following methods:
    @Override
    public RawError createTask(RawTask message) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError removeTask(Long taskId) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError updateTask(RawTask message) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError getTask(Long taskId) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError setTask(RawTask task) {
        for (int i = 0; i < task.boards.length; i++) {
            if (task.boards[i] == openedProject.getProjectId()) {
                openedProject.addTask(new Task(task));
            }
        }
        return null;
    }

    @Override
    public RawError getServerTaskList(Object message) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError setSession(RawSession session) {
        clientSession = new Session(session.sessionKey, -1, -1, connection.getMyIP(), connection.getMyPort(), connection.getOtherIP(), connection.getOtherPort());
        if (session.sessionKey != null && session.sessionKey.length == 32)
            tui.setDisplayUsername(username);
        else
            tui.setDisplayUsername("");
        return null;
    }

    @Override
    public RawError login(RawLogin message) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError getProjectList() {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError setProjectList(RawProjectNameList message) {
        projectNameList = message;
        tui.setProjects(Arrays.asList(projectNameList.projectNames));
        return null;
    }

    @Override
    public RawError getProject(Long message) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError setProject(RawProject message) {
        System.out.println("Received project data");
        openedProject = new ClientProject(message);
        return null;
    }

    @Override
    public RawError updateProject(RawProject rawProject) {
        return new RawError("Invalid request on client side");
    }

    @Override
    public RawError userInfo(RawUser rawUser) {
        return new RawError("Invalid request on client side");
    }
}
