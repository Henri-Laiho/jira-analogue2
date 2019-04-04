package common;

import data.RawUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class User {
    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public User(RawUser rawUser) {
        this.rawUser = rawUser;
        this.userId = rawUser.userId;
        this.username = rawUser.username;
        this.passwordHash = rawUser.passwordHash;
        this.userEmail = rawUser.userEmail;
        this.lastOnlineMS = rawUser.lastOnlineMS;
        this.projects = new ArrayList<>();
        this.projectRights = new HashMap<>();
        this.friendList = new ArrayList<>();

        if (rawUser.friendList != null)
            Arrays.sort(rawUser.friendList);
        if (rawUser.projects != null)
            Arrays.sort(rawUser.projects);
    }

    public void initialize(List<User> users, List<Project> projects) {
        if (rawUser.friendList != null) {
            for (User user : users) {
                if (Arrays.binarySearch(rawUser.friendList, user.getUserId()) >= 0) {
                    friendList.add(user);
                }
            }
        }

        if (rawUser.projects != null) {
            int[] indexes = new int[rawUser.projects.length];
            int i = 0;
            for (Project project : projects) {
                int search;
                if ((search = Arrays.binarySearch(rawUser.projects, project.getProjectId())) >= 0) {
                    projects.add(project);
                    indexes[i] = search;
                    i++;
                }
            }

            if (rawUser.projectRights != null) {
                i = 0;
                for (Project project : this.projects) {
                    projectRights.put(project, rawUser.projectRights[indexes[i]]);
                    i++;
                }
            }
        }

        rawUser = null;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void addProject(Project project) {
        projects.add(project);
    }

    public List<Project> getProjects() {
        return projects;
    }

    private RawUser rawUser;

    private long userId;
    private String username;
    private byte[] passwordHash = null;
    private String userEmail = null;
    private Long lastOnlineMS = null;
    private List<Project> projects = null;
    private HashMap<Project, Integer> projectRights = null;
    private List<User> friendList = null;
}
