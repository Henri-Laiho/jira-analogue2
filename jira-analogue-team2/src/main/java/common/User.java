package common;

import data.RawUser;
import server.ServerUser;

import java.util.*;

public class User {

    private RawUser rawUser;

    private long userId;
    private String username;
    private byte[] passwordHash = null;
    private String userEmail = null;
    private Long lastOnlineMS = null;
    private Map<Long, Project> projects = null;  // KNOW WHEN TO UPDATE THIS
    private Map<Project, Integer> projectRights = null;  // KNOW WHEN TO UPDATE THIS
    private List<User> friendList = null;  // KNOW WHEN TO UPDATE THIS

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
        this.projects = new HashMap<>();
        this.projectRights = new HashMap<>();
        this.friendList = new ArrayList<>();

        if (rawUser.friendList != null)
            Arrays.sort(rawUser.friendList);
        if (rawUser.projects != null)
            Arrays.sort(rawUser.projects);
    }

    public void initialize(Collection<ServerUser> users, Collection<Project> projects) {
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
                    this.projects.put(project.getProjectId(), project);
                    indexes[i] = search;
                    i++;
                }
            }

            if (rawUser.projectRights != null) {
                i = 0;
                for (Project project : this.projects.values()) {
                    projectRights.put(project, rawUser.projectRights[indexes[i]]);
                    i++;
                }
            }
        }

        rawUser = null;
    }

    public RawUser toRawUser() {
        long[] projectIds = new long[projects.size()];
        int[] projectRightsEnums = new int[projects.size()];

        int i = 0;
        for (Project project : projects.values()) {
            projectIds[i] = project.getProjectId();
            projectRightsEnums[i] = projectRights.get(project);
            i++;
        }

        long[] friendsIds = friendList.stream().mapToLong(User::getUserId).toArray();

        return new RawUser(userId, username, passwordHash, userEmail, lastOnlineMS, projectIds, projectRightsEnums, friendsIds);
    }


    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void addProject(Project project) {
        projects.put(project.getProjectId(), project);
    }

    public Map<Long, Project> getProjects() {
        return projects;
    }

    public int getRightsInProject(Project project) {
        if (projectRights != null)
            return projectRights.get(project);
        // return no rights
        return 0;
    }

    public boolean isInitialized() {
        return rawUser == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
