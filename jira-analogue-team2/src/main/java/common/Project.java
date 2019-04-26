package common;

import data.RawProject;
import data.RawProjectNameList;
import data.RawTask;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private static final long MAX_TASKS = Short.MAX_VALUE;

    private long projectId = -1;
    private List<Task> tasklist = new ArrayList<>(); // KNOW WHEN TO UPDATE THIS
    private String projectName = null;
    private String repositoryUrl = null;
    private long lastTaskID = -1;

    public Project(RawProject data) {
        projectId = data.projectId;
        projectName = data.projectName;
        repositoryUrl = data.repositoryUrl;

        for (RawTask rawTask : data.tasks) {
            tasklist.add(new Task(rawTask));
        }
    }

    public RawProject toRawProject() {
        RawTask[] rawTasks = new RawTask[tasklist.size()];
        int i = 0;
        for (Task task : tasklist) {
            rawTasks[i] = task.toRawTask();
            i++;
        }
        return new RawProject(projectId, rawTasks, projectName, repositoryUrl);
    }

    public void initialize(List<User> users, List<Project> projects) {
        for (Task task : tasklist) {
            task.initialize(tasklist, users, projects);
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

    public long getNewValidTaskId() {
        long newID = lastTaskID;
        while (true) {
            newID++;
            boolean valid = true;
            for (Task task : tasklist) {
                if (task.getTaskId() == newID) {
                    valid = false;
                    break;
                }
            }
            if (valid) break;

            if (newID == MAX_TASKS)
                newID = 0;
            if (newID == lastTaskID - 1)
                return -1;
        }
        lastTaskID = newID;
        return newID;
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
