package client;

import common.Project;
import common.Task;
import data.RawProject;
import data.RawTask;

public class ClientProject extends Project {
    public ClientProject(RawProject data) {
        super(data);
        for (RawTask rawTask : data.tasks) {
            Task task = new Task(rawTask);
            tasklist.put(task.getTaskId(), task);
        }
    }

    public void addTask(Task task) {
        tasklist.put(task.getTaskId(), task);
    }
}
