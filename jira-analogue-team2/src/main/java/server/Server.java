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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

class Server implements Runnable {

    private static final long MAX_SESSIONS = 1024;
    private static final long MAX_USERS = 65536;
    private static final long MAX_TASKS = 4294967296L;
    private static final long MAX_PROJECTS = 16777216;
    private static final String DATA_FILE_PATH = "SERVER_DATA.json";

    private final int port;
    private Map<Long, Project> projects = new HashMap<>();
    private Map<Long, Task> tasks = new HashMap<>();
    private Map<Long, ServerUser> users = new HashMap<>();
    //private HashMap<User, byte[]> userSalts = new HashMap<>();
    private Map<Long, SessionForClient> sessions = new HashMap<>();
    private long lastSessionID = -1;
    private boolean running = false;

    Map<Long, Project> getProjects() {
        return projects;
    }

    Map<Long, ServerUser> getUsers() {
        return users;
    }

    Map<Long, Task> getTasks() {
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
        ServerUser newUser;
        try {
            newUser = new ServerUser(new RawServerUser(
                    getNewValidUserID(), userName, SecurityHelper.passwordToHash(password, salt),
                    email, System.currentTimeMillis(), null, null, null, salt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
            //return null;
        }

        users.put(newUser.getUserId(), newUser);
        return newUser;
    }

    SessionForClient getSessionWithKey(byte[] sesskey) {
        for (SessionForClient value : sessions.values()) {
            if (Arrays.equals(value.getSession().getSessionKey(), sesskey))
                return value;
        }
        return null;
    }

    void addSession(SessionForClient session) {
        if (session.getSession() != null)
            sessions.put(session.getSessionID(), session);
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
            projects = rawData.rawProjects.stream().map(rp -> new Project(rp)).collect(Collectors.toMap(Project::getProjectId, project -> project));
            tasks = rawData.rawTasks.stream().map(rp -> new Task(rp)).collect(Collectors.toMap(Task::getTaskId, task -> task));
            users = rawData.rawUsers.stream().map(ru -> new ServerUser(ru)).collect(Collectors.toMap(ServerUser::getUserId, serverUser -> serverUser));

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
                new RawTask(3, false, "A Task 2", "do something again 2", 2, 201L,
                        1576800000000L, 1545264000000L, 4L, null, null),
                new RawTask(4, true, "A Completed Task 2", "do something 2", 1, 201L,
                        1576800000000L, 1545264000000L, -1L, null, null)
        };
        for (RawTask rawTask : tasks1) {
            tasks.put(rawTask.taskId, new Task(rawTask));
        }
        for (RawTask rawTask : tasks2) {
            tasks.put(rawTask.taskId, new Task(rawTask));
        }

        long[] taskids1 = {1, 2};
        long[] taskids2 = {3, 4};

        projects.put(10L, new Project(new RawProject(10, tasks1, taskids1,"Project Hello World", "no repo")));
        projects.put(11L, new Project(new RawProject(11, tasks2, taskids2,"Hello World Project", "no repository")));

        User jaan = createUser("Jaan Tamm", "jaan123", "jaan.tamm@mail.ee");

        jaan.addProject(projects.get(10L));
        jaan.addProject(projects.get(11L));
    }

    void saveData() {
        var rawProjects = projects.values().stream().map(Project::toRawProject).collect(Collectors.toList());
        var rawTasks = tasks.values().stream().map(Task::toRawTask).collect(Collectors.toList());
        var rawUsers = users.values().stream().map(ServerUser::toRawUser).collect(Collectors.toList());
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

    private void initialize() {
        for (Project project : projects.values()) {
            project.initialize(tasks);
        }
        for (Task task : tasks.values()) {
            task.initialize(tasks.values(), users.values(), projects.values());
        }
        for (User user : users.values()) {
            user.initialize(users.values(), projects.values());
        }
    }

    /**
     * @return a new valid session ID or -1 if the max number of session has been reached.
     */
    long getNewValidSessID() {
        lastSessionID = getNewValidID(sessions, MAX_SESSIONS, lastSessionID);
        return lastSessionID;
    }

    /**
     * @return a new valid user ID or -1 if the max number of users has been reached.
     */
    long getNewValidUserID() {
        return getNewValidID(users, MAX_USERS);
    }

    long getNewValidTaskID() {
        return getNewValidID(tasks, MAX_TASKS);
    }

    long getNewValidProjectID() {
        return getNewValidID(projects, MAX_PROJECTS);
    }

    private static long getNewValidID(Map<Long, ?> data, long max) {
        return getNewValidID(data, max, 0);
    }

    private static long getNewValidID(Map<Long, ?> data, long max, long start) {
        long newID = start;
        while (data.containsKey(newID)) {

            newID++;
            if (newID == max)
                newID = 0;
            if (newID == start)
                return -1;
        }
        return newID;
    }

    byte[] getUserSalt(User user) {
        return users.get(user.getUserId()).getSalt();
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
