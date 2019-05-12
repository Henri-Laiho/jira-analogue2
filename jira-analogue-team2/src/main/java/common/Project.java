package common;

import data.RawProject;
import data.RawProjectNameList;
import data.RawTask;

import java.util.*;

public class Project {

    private static final long MAX_TASKS = Short.MAX_VALUE;

    private long projectId = -1;
    protected Map<Long, Task> tasklist = new HashMap<>(); // KNOW WHEN TO UPDATE THIS
    private String projectName;
    private String repositoryUrl;
    private long lastTaskID = -1;
    private RawProject data;

    public Project(RawProject data) {
        projectId = data.projectId;
        projectName = data.projectName;
        repositoryUrl = data.repositoryUrl;
        this.data = data;
    }

    public void update(Project other, Map<Long, Task> tasks) {
        projectId = other.projectId;
        projectName = other.projectName;
        repositoryUrl = other.repositoryUrl;
        this.data = other.data;
        initialize(tasks);
    }

    public RawProject toRawProject() {
        RawTask[] rawTasks = new RawTask[tasklist.size()];
        long[] taskIds = new long[tasklist.size()];
        int i = 0;
        for (Task task : tasklist.values()) {
            rawTasks[i] = task.toRawTask();
            taskIds[i] = task.getTaskId();
            i++;
        }
        return new RawProject(projectId, rawTasks, taskIds, projectName, repositoryUrl);
    }

    public void initialize(Map<Long, Task> tasks) {
        if (data.tasks != null) {
            for (RawTask rawTask : data.tasks) {
                Task task = tasks.get(rawTask.taskId);
                tasklist.put(task.getTaskId(), task);
            }
        }
        else if (data.taskIds != null) {
            for (long id : data.taskIds) {
                Task task = tasks.get(id);
                tasklist.put(task.getTaskId(), task);
            }
        }
        data = null;
    }

    public long getProjectId() {
        return projectId;
    }

    public Map<Long, Task> getTasklist() {
        return tasklist;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public long getNewValidTaskId() {
        long newID = lastTaskID;
        while (true) {
            newID++;
            boolean valid = true;
            for (Task task : tasklist.values()) {
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

    public static RawProjectNameList getProjectNameList(Map<Long, Project> projects) {
        String[] names = new String[projects.size()];
        long[] ids = new long[projects.size()];

        int i = 0;
        for (Project project : projects.values()) {
            names[i] = project.getProjectName();
            ids[i] = project.getProjectId();
            i++;
        }

        return new RawProjectNameList(names, ids);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }
}
