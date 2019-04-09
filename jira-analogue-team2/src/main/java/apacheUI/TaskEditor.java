package apacheUI;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import common.Task;

import java.util.List;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void taskEdited(Task task);
    }

    private TaskEditedListener listener = null;
    private ActionListBox actionListBox;
    private List<Task> tasks;

    public TaskEditor(Task task, int userRights, List<Task> tasks, TaskEditedListener listener) {
        setCloseWindowWithEscape(true);
        Panel panel = new Panel();
        this.actionListBox = new ActionListBox();
        this.actionListBox.takeFocus();
        this.tasks = tasks;
        this.listener = listener;

        panel.addComponent(actionListBox);
    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }

    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
