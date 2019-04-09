package apacheUI;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import java.util.List;

class ProjectEditor extends BasicWindow {
    interface TaskSelectedListener {
        void taskSelected(int taskIndex);

        void createTask();
    }

    private TaskSelectedListener listener;
    private ActionListBox actionListBox;
    private Table<String> taskData;

    ProjectEditor(TaskSelectedListener listener) {
        setCloseWindowWithEscape(true);
        this.listener = listener;

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        actionListBox = new ActionListBox();
        taskData = new Table<>();
        taskData.setVisibleColumns(3);

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

    void setTaskList(List<String> taskTitles, List<String> priorities, List<String> deadlines, List<String> completed) {
        actionListBox.clearItems();

        actionListBox.addItem("Create task", () -> {
            if (listener != null)
                listener.createTask();
        });

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

        tableModel.addColumn("Is Done", (String[]) completed.toArray());
        tableModel.addColumn("Priority", (String[]) priorities.toArray());
        tableModel.addColumn("Deadline", (String[]) deadlines.toArray());

        taskData.setTableModel(tableModel);

        actionListBox.takeFocus();
    }


}
