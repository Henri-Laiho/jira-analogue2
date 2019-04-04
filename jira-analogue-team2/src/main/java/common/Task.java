package common;

import data.RawTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Task {

    public Task(RawTask rawTask) {
        this.rawTask = rawTask;
        this.taskId = rawTask.taskId;
        this.isCompleted = rawTask.isCompleted;
        this.title = rawTask.title;
        this.description = rawTask.description;
        this.priority = rawTask.priority;
        this.deadline = new Date(rawTask.deadlineMS);
        this.dateCreated = new Date(rawTask.dateCreatedMS);

        if (rawTask.assignedEmployees != null)
            Arrays.sort(rawTask.assignedEmployees);
    }

    public void initialize(List<Task> taskList, List<User> users) {
        for (Task task : taskList) {
            if (task.taskId == rawTask.masterTaskId && task.taskId != taskId) {
                this.masterTask = task;
                break;
            }
        }

        if (rawTask.assignedEmployees != null) {
            for (User user : users) {
                int i;
                if ((i = Arrays.binarySearch(rawTask.assignedEmployees, user.getUserId())) >= 0) {
                    this.employees.add(user);
                }
                if (user.getUserId() == rawTask.createdBy) {
                    createdBy = user;
                }
            }
        }
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

}
