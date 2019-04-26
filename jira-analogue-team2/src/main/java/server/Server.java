package server;

import auth.SecurityHelper;
import common.Connection;
import common.Project;
import common.User;
import data.RawProject;
import data.RawTask;
import data.RawUser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

class Server implements Runnable {

    private static final long MAX_SESSIONS = 1024;
    private static final long MAX_USERS = 65536;

    private final int port;
    private List<Project> projects = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private HashMap<User, byte[]> userSalts = new HashMap<>();
    private List<SessionForClient> sessions = new ArrayList<>();
    private long lastSessionID = -1;
    private boolean running = false;

    List<Project> getProjects() {
        return projects;
    }

    List<User> getUsers() {
        return users;
    }

    Server() {
        this.port = Connection.DEFAULT_PORT;
    }

    Server(int port) {
        this.port = port;
    }

    User createUser(String userName, String password, String email) {
        byte[] salt = SecurityHelper.generateSalt();
        User newUser = null;
        try {
            newUser = new User(new RawUser(
                    getNewValidUserID(), userName, SecurityHelper.passwordToHash(password, salt),
                    email, System.currentTimeMillis(), null, null, null));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
            //return null;
        }
        userSalts.put(newUser, salt);
        users.add(newUser);
        return newUser;
    }

    void loadData() {
        // TODO: load data from files

        // test data:
        RawTask[] tasks1 = {
                new RawTask(1, false, "A Task", "do something again", 1, 200L,
                        1576800000000L, 1545264000000L, -1L, null, null),
                new RawTask(2, true, "A Completed Task", "do something", 2, 200L,
                        1576800000000L, 1545264000000L, -1L, null, null)
        };

        RawTask[] tasks2 = {
                new RawTask(3, false, "A Task", "do something again", 2, 201L,
                        1576800000000L, 1545264000000L, 4L, null, null),
                new RawTask(4, true, "A Completed Task", "do something", 1, 201L,
                        1576800000000L, 1545264000000L, -1L, null, null)
        };
        projects.add(new Project(new RawProject(10, tasks1, "Project Hello World", "no repo")));
        projects.add(new Project(new RawProject(11, tasks2, "Hello World Project", "no repository")));

        createUser("Jaan Tamm", "jaan123", "jaan.tamm@mail.ee");

        users.get(0).addProject(projects.get(0));
        users.get(0).addProject(projects.get(1));

        initialize();
    }

    void initialize() {
        for (Project project : projects) {
            project.initialize(users, projects);
        }
        for (User user : users) {
            user.initialize(users, projects);
        }
    }

    /**
     * @return a new valid session ID or -1 if the max number of session has been reached.
     */
    long getNewValidSessID() {
        long newID = lastSessionID;
        while(true) {
            newID++;
            boolean valid = true;
            for (SessionForClient session : sessions) {
                if (session.getSessionID() == newID) {
                    valid = false;
                    break;
                }
            }
            if (valid) break;

            if (newID == MAX_SESSIONS)
                newID = 0;
            if (newID == lastSessionID-1)
                return -1;
        }
        lastSessionID = newID;
        return newID;
    }

    /**
     * @return a new valid user ID or -1 if the max number of users has been reached.
     */
    long getNewValidUserID() {
        long newID = 0;
        while(true) {
            boolean valid = true;
            for (User user : users) {
                if (user.getUserId() == newID) {
                    valid = false;
                    break;
                }
            }
            if (valid) break;

            newID++;
            if (newID == MAX_USERS)
                return -1;
        }
        return newID;
    }

    byte[] getUserSalt(User user) {
        return userSalts.getOrDefault(user, null);
    }


    @Override
    public void run() {

        // allocate port on all available IP addresses
        try (ServerSocket ss = new ServerSocket(port)) {

            // setup thread pool
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(32);

            // wait for an incoming connection
            System.out.println("Listening on port " + port);
            running = true;
            while (running) {
                try {
                    Socket socket = ss.accept();
                    SessionForClient session = new SessionForClient(this, socket);

                    executor.submit(session);

                    //Thread thread = new Thread(session);
                    //thread.start();
                    System.out.println("New Connection on port " + port + " from " + socket.getInetAddress().getHostName() + ".");
                } catch (IOException e) {
                    System.out.println("Stopping server.");
                    running = false;
                    throw new RuntimeException(e);
                    //break;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to start server.");
            running = false;
            throw new RuntimeException(e);
        }
    }

    public void runSingleThread() {
        // allocate port on all available IP addresses
        try (ServerSocket ss = new ServerSocket(port)) {

            // wait for an incoming connection
            System.out.println("Listening on port " + port);
            running = true;
            while (running) {
                try {
                    Socket socket = ss.accept();
                    SessionForClient session = new SessionForClient(this, socket);

                    session.runSingleRequest();

                    System.out.println("New Connection on port " + port + " from " + socket.getInetAddress().getHostName() + ".");
                } catch (IOException e) {
                    System.out.println("Stopping server.");
                    running = false;
                    throw new RuntimeException(e);
                    //break;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to start server.");
            running = false;
            throw new RuntimeException(e);
        }
    }

}
