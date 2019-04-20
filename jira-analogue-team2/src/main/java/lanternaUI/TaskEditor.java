package lanternaUI;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import common.Task;

import java.util.List;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void partSelected(int taskIndex);
        //void taskEdited(Task task);
    }


    private TaskEditedListener listener = null;
    private ActionListBox actionListBox;
    private List<Task> tasks;
    private Table<String> taskTable;


    public TaskEditor(Task task, int userRights, List<Task> tasks, TaskEditedListener listener) {
        setCloseWindowWithEscape(true);
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(4));

        this.actionListBox = new ActionListBox();
        //this.actionListBox.takeFocus();
        //int OptionCount = actionListBox.getItemCount();


        this.tasks = tasks;
        this.listener = listener;
        panel.addComponent(new Label("Select column:"));
        panel.addComponent(new EmptySpace());


        //List<String> taskData = Arrays.asList(task.getDescription(), String.valueOf(task.getPriority()), String.valueOf(task.getDeadline())); //to easily edit/display in column

        taskTable = new Table<>("Description", "Priority", "Deadline");
        TableModel<String> taskDataInfo = new TableModel<>();
        taskDataInfo.addColumn("Description", null);
        taskDataInfo.addColumn("Priority", null);
        taskDataInfo.addColumn("Deadline", null);
        taskDataInfo.addRow(task.getDescription(), String.valueOf(task.getPriority()), String.valueOf(task.getDeadline()));


        //selects which part to edit (editing not implemented)
        for (int i = 0; i < taskDataInfo.getColumnCount(); i++) {
            int selectedI = i;
            actionListBox.addItem(String.valueOf(taskDataInfo.getColumnLabel(i)), () -> {
                if (listener != null) {
                    listener.partSelected(selectedI);
                }
            });
        }


        taskTable.setTableModel(taskDataInfo);
        taskTable.setEnabled(false);
        panel.addComponent(actionListBox);
        panel.addComponent(new EmptySpace());
        panel.addComponent(taskTable);
        panel.addComponent(new EmptySpace());
        setComponent(panel);


    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
