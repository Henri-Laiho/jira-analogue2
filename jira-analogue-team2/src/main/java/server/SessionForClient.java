package server;

import auth.SecurityHelper;
import common.Connection;
import common.Project;
import common.Task;
import common.User;
import data.*;
import messages.JiraMessageHandler;
import messages.MessageType;
import messages.Session;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class SessionForClient implements Runnable, JiraMessageHandler {
    private Connection connection;
    private Session session;
    private Server server;
    private User user;
    private Map<Long, Task> userTasks;
    private Project openedProject;

    SessionForClient(Server server, Socket socket) throws IOException {
        this.server = server;
        this.connection = new Connection(null, this, socket);
    }

    private void getOldSessionData(SessionForClient oldSession) {
        openedProject = oldSession.openedProject;
        userTasks = oldSession.userTasks;
    }

    Session getSession() {
        return session;
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
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                System.exit(-1);
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
        message.taskId = server.getNewValidTaskID();

        if (message.taskId == -1)
            return new RawError("Maximum number of tasks reached.");

        for (long board : message.boards) {
            if (!user.getProjects().containsKey(board)) {
                return new RawError("You are trying to add a task to a project where you are not a collaborator.");
            }
        }
        Task newTask = new Task(message);
        newTask.initialize(server.getTasks().values(), server.getUsers().values(), server.getProjects().values());
        server.getTasks().put(newTask.getTaskId(), newTask);

        for (Project project : newTask.getProjects()) {
            project.getTasklist().put(newTask.getTaskId(), newTask);
        }

        try {
            connection.sendMessage(null, MessageType.RESPONSE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public RawError removeTask(Long message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError updateTask(RawTask message) {
        if (openedProject == null)
            return new RawError("You have not opened a project yet.");

        Task newTask = new Task(message);
        if (server.getTasks().containsKey(newTask.getTaskId())) {
            Task task = server.getTasks().get(newTask.getTaskId());
            if (openedProject.getTasklist().containsKey(task.getTaskId())) {
                task.update(newTask, server.getTasks().values(), server.getUsers().values(), server.getProjects().values());
                try {
                    connection.sendMessage(null, MessageType.RESPONSE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            } else {
                return new RawError("This task is not in the currently opened project.");
            }
        } else {
            return new RawError("Can't update task: The task does not exist.");
        }
    }

    @Override
    public RawError getTask(Long taskId) {
        if (userTasks == null)
            return new RawError("You must be logged in.");
        if (userTasks.containsKey(taskId)) {
            try {
                connection.sendMessage(userTasks.get(taskId).toRawTask(), MessageType.SETTASK);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return new RawError("This task is not in your projects");
    }

    @Override
    public RawError setTask(RawTask task) {
        return new RawError("Invalid request on server side");
    }

    @Override
    public RawError getServerTaskList(Object message) {
        return new RawError("Not yet implemented :(");
    }

    @Override
    public RawError setSession(RawSession session) {
        SessionForClient sess = server.getSessionWithKey(session.sessionKey);
        if (sess != null && sess.getSession().isValid()) {
            getOldSessionData(sess);
            return null;
        }
        return new RawError("Session not valid.");
    }

    @Override
    public RawError login(RawLogin log) {
        for (User user : server.getUsers().values()) {
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
                        connection.sendMessage(new RawSession(session.getSessionKey()), MessageType.SETSESSION);
                        this.user = user;

                        server.addSession(this);

                        this.userTasks = new HashMap<>();
                        for (Project project : user.getProjects().values()) {
                            userTasks.putAll(project.getTasklist());
                        }

                        // return no error
                        return null;
                    } else
                        return new RawError("Wrong password or username.");
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    this.session = null;
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

        if (server.getUsers().containsKey(session.getUserId())) {
            User user = server.getUsers().get(session.getUserId());
            Map<Long, Project> projects = user.getProjects();

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
        return new RawError("User is missing from the database.");
    }

    @Override
    public RawError setProjectList(RawProjectNameList message) {
        return new RawError("Invalid request on server side");
    }

    @Override
    public RawError getProject(Long projectId) {
        if (session != null && session.isValid()) {
            if (user.getProjects().containsKey(projectId)) {
                Project project = user.getProjects().get(projectId);
                try {
                    connection.sendMessage(project.toRawProject(), MessageType.SETPROJECT);
                    openedProject = project;
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                    //return new RawError(...)
                }
            }
        }
        return new RawError("The requested project doesn't exist or you are not a collaborator.");
    }

    @Override
    public RawError setProject(RawProject message) {
        return new RawError("Invalid request on server side");
    }

    @Override
    public RawError updateProject(RawProject rawProject) {
        if (session != null && session.isValid()) {
            if (user.getProjects().containsKey(rawProject.projectId)) {
                Project newProject = new Project(rawProject);
                Project project = user.getProjects().get(rawProject.projectId);
                project.update(newProject, server.getTasks());
                try {
                    connection.sendMessage(null, MessageType.RESPONSE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }
        return new RawError("The requested project doesn't exist or you are not a collaborator.");
    }

    @Override
    public RawError userInfo(RawUser rawUser) {
        return new RawError("Not yet implemented :(");
    }
}
