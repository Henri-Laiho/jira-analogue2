package common;

import data.RawProject;
import data.RawProjectNameList;
import data.RawTask;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private long projectId = -1;
    private List<Task> tasklist = new ArrayList<>();
    private String projectName = null;
    private String repositoryUrl = null;

    public Project(RawProject data) {
        projectId = data.projectId;
        projectName = data.projectName;
        repositoryUrl = data.repositoryUrl;

        for (RawTask rawTask : data.tasks) {
            tasklist.add(new Task(rawTask));
        }
    }

    public void initialize(List<User> users) {
        for (Task task : tasklist) {
            task.initialize(tasklist, users);
        }
    }

    public long getProjectId() {
        return projectId;
    }

    public List<Task> getTasklist() {
        return tasklist;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public static RawProjectNameList getProjectNameList(List<Project> projects) {
        String[] names = new String[projects.size()];
        long[] ids = new long[projects.size()];

        int i = 0;
        for (Project project : projects) {
            names[i] = project.getProjectName();
            ids[i] = project.getProjectId();
            i++;
        }

        return new RawProjectNameList(names, ids);
    }
}
