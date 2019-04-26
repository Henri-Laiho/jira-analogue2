package common;

import data.RawUser;

import java.util.*;

public class User {

    private RawUser rawUser;

    private long userId;
    private String username;
    private byte[] passwordHash = null;
    private String userEmail = null;
    private Long lastOnlineMS = null;
    private List<Project> projects = null;  // KNOW WHEN TO UPDATE THIS
    private HashMap<Project, Integer> projectRights = null;  // KNOW WHEN TO UPDATE THIS
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
                    this.projects.add(project);
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

    // allProjects must be in order.
    public RawUser toRawUser(List<Project> allProjects) {

        long[] projectIds = projects.stream().mapToLong(p -> p.getProjectId()).toArray();

        int[] projectRightsEnums = allProjects.stream().mapToInt(p -> {
            Integer rights = projectRights.get(p);
            if (rights == null) {
                return 0;
            }
            return rights;
        }).toArray();

        long[] friendsIds = friendList.stream().mapToLong(u -> u.getUserId()).toArray();

        return new RawUser(userId, username, passwordHash, userEmail, lastOnlineMS, projectIds, projectRightsEnums, friendsIds);
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

    public int getRightsInProject(Project project) {
        if (projectRights != null)
            return projectRights.get(project);
        // return no rights
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}
