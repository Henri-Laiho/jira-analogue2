package LanternaUI;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import common.Task;

import java.util.Arrays;
import java.util.List;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void taskEdited(Task task);
    }

    private TaskEditedListener listener = null;
    private ActionListBox actionListBox;
    private List<Task> tasks;
    private TableModel<String> taskTable;


    public TaskEditor(Task task, int userRights, List<Task> tasks, TaskEditedListener listener) {
        setCloseWindowWithEscape(true);
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(4));

        this.actionListBox = new ActionListBox();
        this.actionListBox.takeFocus();
        int OptionCount = actionListBox.getItemCount();

        this.tasks = tasks;
        this.listener = listener;
        panel.addComponent(new Label("Editing task"));
        panel.addComponent(new EmptySpace());

        List<String> taskData = Arrays.asList(task.getDescription(), String.valueOf(task.getPriority()), String.valueOf(task.getDeadline())); //to easily edit/display in column
        Table<Object> taskDataInfo = new Table<>("Description", "Priority", "Deadline");

        taskDataInfo.setEnabled(true);
        taskDataInfo.setSelectedColumn(OptionCount);
        //fix me

        panel.addComponent(actionListBox);
        panel.addComponent(taskDataInfo);
        setComponent(panel);


    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }

    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
