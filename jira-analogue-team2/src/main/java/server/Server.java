package server;

import auth.SecurityHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import common.Connection;
import common.Project;
import common.Task;
import common.User;
import data.RawProject;
import data.RawTask;
import data.RawUser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

class Server implements Runnable {

    private static final long MAX_SESSIONS = 1024;
    private static final long MAX_USERS = 65536;
    private static final String DATA_FILE_PATH = "SERVER_DATA.json";

    private final int port;
    private List<Project> projects = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<ServerUser> users = new ArrayList<>();
    //private HashMap<User, byte[]> userSalts = new HashMap<>();
    private List<SessionForClient> sessions = new ArrayList<>();
    private long lastSessionID = -1;
    private boolean running = false;

    List<Project> getProjects() {
        return projects;
    }

    List<ServerUser> getUsers() {
        return users;
    }

    List<Task> getTasks() {
        return tasks;
    }

    Server() {
        this.port = Connection.DEFAULT_PORT;
    }

    Server(int port) {
        this.port = port;
    }

    ServerUser createUser(String userName, String password, String email) {
        byte[] salt = SecurityHelper.generateSalt();
        ServerUser newUser = null;
        try {
            newUser = new ServerUser(new RawServerUser(
                    getNewValidUserID(), userName, SecurityHelper.passwordToHash(password, salt),
                    email, System.currentTimeMillis(), null, null, null, salt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
            //return null;
        }
        //userSalts.put(newUser, salt);
        users.add(newUser);
        return newUser;
    }

    void loadData() {
        Gson gson = new Gson();
        try {
            String json = Files.readString(Path.of(DATA_FILE_PATH));
            RawServerData rawData = gson.fromJson(json, RawServerData.class);


            /*if (rawData.userSalts.size() != rawData.rawUsers.size()) {
                throw new JsonSyntaxException("Corrupt file (rawData.userSalts.size() != rawData.rawUsers.size()).");
            }*/

            // Convert types
            projects = rawData.rawProjects.stream().map(rp -> new Project(rp)).collect(Collectors.toList());
            tasks = rawData.rawTasks.stream().map(rp -> new Task(rp)).collect(Collectors.toList());
            users = rawData.rawUsers.stream().map(ru -> new ServerUser(ru)).collect(Collectors.toList());

            /*for (int i = 0; i < users.size(); i++) {
                userSalts.put(users.get(i), rawData.userSalts.get(i));
            }*/

        } catch (IOException e) {
            System.out.println("ERROR: Can't open server data file");
            e.printStackTrace();
            System.out.println("Loading built in test data.");
            loadTestData();
        } catch (JsonSyntaxException e) {
            System.out.println("ERROR: Server data file is corrupt.");
            e.printStackTrace();
            System.out.println("Loading built in test data.");
            loadTestData();
        }
        initialize();
        if (!Files.exists(Path.of(DATA_FILE_PATH))) {
            System.out.println("Saving new data file.");
            saveData();
        }

    }

    void loadTestData() {
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
        for (RawTask rawTask : tasks1) {
            tasks.add(new Task(rawTask));
        }
        for (RawTask rawTask : tasks2) {
            tasks.add(new Task(rawTask));
        }
        projects.add(new Project(new RawProject(10, tasks1, "Project Hello World", "no repo")));
        projects.add(new Project(new RawProject(11, tasks2, "Hello World Project", "no repository")));

        createUser("Jaan Tamm", "jaan123", "jaan.tamm@mail.ee");

        users.get(0).addProject(projects.get(0));
        users.get(0).addProject(projects.get(1));
    }

    void saveData() {
        var rawProjects = projects.stream().map(p -> p.toRawProject()).collect(Collectors.toList());
        var rawTasks = tasks.stream().map(p -> p.toRawTask()).collect(Collectors.toList());
        var rawUsers = users.stream().map(u -> u.toRawUser(projects)).collect(Collectors.toList());
        //var saltsData = users.stream().map(u -> userSalts.get(u)).collect(Collectors.toList());

        var rawData = new RawServerData();

        rawData.rawProjects = rawProjects;
        rawData.rawTasks = rawTasks;
        rawData.rawUsers = rawUsers;
        //rawData.userSalts = saltsData;


        var gson = new GsonBuilder().setPrettyPrinting().create(); // For easyer debugging.
        String text = gson.toJson(rawData);

        try {
            Files.writeString(Path.of(DATA_FILE_PATH), text);
        } catch (IOException e) {
            System.out.println("Can't save data file.");
            e.printStackTrace();
            System.out.println("Ignoring previous error.");
        }


    }

    void initialize() {
        for (Project project : projects) {
            project.initialize(tasks);
        }
        for (Task task : tasks) {
            task.initialize(tasks, users, projects);
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
        while (true) {
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
            if (newID == lastSessionID - 1)
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
        while (true) {
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
        return users.get(users.indexOf(user)).getSalt();
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
