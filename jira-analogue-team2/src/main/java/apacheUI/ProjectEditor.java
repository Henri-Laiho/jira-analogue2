package apacheUI;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import java.util.Arrays;
import java.util.List;

class ProjectEditor extends BasicWindow {
    interface TaskSelectedListener {
        void taskSelected(int taskIndex);

        void createTask();
    }

    private TaskSelectedListener listener;
    private ActionListBox actionListBox;
    private Table<String> taskData;
    private Panel panel;

    ProjectEditor(TaskSelectedListener listener) {
        setCloseWindowWithEscape(true);
        this.listener = listener;

        panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        actionListBox = new ActionListBox();
        taskData = new Table<>("Is Done", "Priority", "Deadline");

        // make not focusable, cursor cannot go into the table
        taskData.setEnabled(false);

        panel.addComponent(new Label("Select a task or add a new task:"));
        panel.addComponent(new EmptySpace());

        panel.addComponent(actionListBox);
        panel.addComponent(taskData);

        setComponent(panel);
    }

    ProjectEditor() {
        this(null);
    }

    void setListener(TaskSelectedListener listener) {
        this.listener = listener;
    }

    void setTaskList(List<String> taskTitles, List<String> priorities, List<String> deadlines, List<String> completed, int selectedTask) {
        actionListBox.clearItems();

        actionListBox.addItem("Create task", () -> {
            if (listener != null)
                listener.createTask();
        });

        int extraOptionCount = actionListBox.getItemCount();
        int i = 0;
        for (String task : taskTitles) {
            int finalI = i;
            actionListBox.addItem(task, () -> {
                if (listener != null)
                    listener.taskSelected(finalI);
            });
            i++;
        }

        TableModel<String> tableModel = new TableModel<>();

        tableModel.addColumn("Is Done", null);
        tableModel.addColumn("Priority", null);
        tableModel.addColumn("Deadline", null);

        for (int j = 0; j < completed.size() && j < priorities.size() && j < deadlines.size(); j++) {
            tableModel.addRow(completed.get(j), priorities.get(j), deadlines.get(j));
        }

        taskData.setTableModel(tableModel);
        taskData.invalidate();

        actionListBox.setSelectedIndex(selectedTask+extraOptionCount);
        actionListBox.takeFocus();
    }

    void setTaskList(List<String> taskTitles, List<String> priorities, List<String> deadlines, List<String> completed) {
        setTaskList(taskTitles, priorities, deadlines, completed, 0);
    }


}
