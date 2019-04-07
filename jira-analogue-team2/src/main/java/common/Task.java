package common;

import data.RawTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;

public class Task {

    public Task(RawTask rawTask) {
        this.rawTask = rawTask;
        this.taskId = rawTask.taskId;
        this.isCompleted = rawTask.isCompleted;
        this.title = rawTask.title;
        this.description = rawTask.description;
        this.priority = rawTask.priority == null ? -1 : rawTask.priority;
        this.deadline = rawTask.deadlineMS == null ? null : new Date(rawTask.deadlineMS);
        this.dateCreated = rawTask.dateCreatedMS == null ? null : new Date(rawTask.dateCreatedMS);

        if (rawTask.assignedEmployees != null)
            Arrays.sort(rawTask.assignedEmployees);

        if (rawTask.boards != null)
            Arrays.sort(rawTask.boards);
    }

    public void initialize(List<Task> taskList, List<User> users, List<Project> projects) {
        if (rawTask.masterTaskId != null)
            for (Task task : taskList) {
                if (task.taskId == rawTask.masterTaskId && task.taskId != taskId) {
                    this.masterTask = task;
                    break;
                }
            }

        if (rawTask.assignedEmployees != null) {
            for (User user : users) {
                if (Arrays.binarySearch(rawTask.assignedEmployees, user.getUserId()) >= 0) {
                    this.employees.add(user);
                }
                if (rawTask.createdBy != null && user.getUserId() == rawTask.createdBy) {
                    createdBy = user;
                }
            }
        }

        if (rawTask.boards != null) {
            for (Project project : projects) {
                if (Arrays.binarySearch(rawTask.boards, project.getProjectId()) >= 0) {
                    this.projects.add(project);
                }
            }
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getTitle() {
        return title;
    }

    public long getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Date getDeadline() {
        return deadline;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Task getMasterTask() {
        return masterTask;
    }

    public List<User> getEmployees() {
        return employees;
    }

    private RawTask rawTask;

    private long taskId;
    private boolean isCompleted;
    private String title;
    private String description;
    private int priority;
    private User createdBy;
    private Date deadline;
    private Date dateCreated;
    private Task masterTask;
    private List<User> employees = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    public RawTask toRawTask() {
        // put user ids to an array
        long[] assignedEmployees = null;
        if (employees != null) {
            assignedEmployees = new long[employees.size()];
            int i = 0;
            for (User employee : employees) {
                assignedEmployees[i] = employee.getUserId();
                i++;
            }
        }

        // put project ids to an array
        long[] boards = null;
        if (projects != null) {
            boards = new long[projects.size()];
            int i = 0;
            for (Project project : projects) {
                boards[i] = project.getProjectId();
                i++;
            }
        }

        // create the RawTask.
        return new RawTask(taskId, isCompleted, title, description, priority, createdBy.getUserId(), deadline == null ? null : deadline.getTime(),
                dateCreated == null ? null : dateCreated.getTime(), masterTask == null ? null : masterTask.taskId, assignedEmployees, boards);
    }
}
