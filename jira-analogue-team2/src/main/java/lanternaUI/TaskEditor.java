package lanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;
import common.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static common.Constants.DATETIME_FORMAT;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void taskEdited(Task task);
    }

    interface TextFieldEditedListener {
        void textFieldEdited(String newValue);
    }

    private static final String[] fieldNames = {"Title", "Description", "Is Done", "Priority", "Deadline"};

    private Panel panel;
    private TaskEditedListener listener;
    private List<Task> tasks;
    private Table<String> taskTable;
    private Label editingFieldNameLabel;
    private TextBox fieldEditor;
    private Button cancelBtn;
    private Button saveBtn;
    private Task task;

    private void editText(int fieldIndex, String initial, boolean multiline, InputFilter filter, TextFieldEditedListener listener) {
        editingFieldNameLabel.setText(fieldNames[fieldIndex]);
        fieldEditor.setText(initial);
        fieldEditor.setPreferredSize(new TerminalSize(20, multiline ? 10 : 1));

        fieldEditor.setInputFilter((interactable, keyStroke) -> {
            if (filter == null || filter.onInput(interactable, keyStroke)) {
                if ((!multiline || fieldEditor.getCaretPosition().getRow() == 0)
                        && keyStroke.getKeyType() == KeyType.ArrowUp
                        || (!multiline || fieldEditor.getCaretPosition().getRow() == fieldEditor.getLineCount() - 1)
                        && keyStroke.getKeyType() == KeyType.ArrowDown
                        || keyStroke.getKeyType() == KeyType.Tab)
                    return false;
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    resetTextField();
                    return false;
                }
                if ((!multiline || keyStroke.isCtrlDown()) && keyStroke.getKeyType() == KeyType.Enter) {
                    if (listener != null)
                        listener.textFieldEdited(fieldEditor.getText());
                    resetTextField();
                    return false;
                }
                return true;
            }
            return false;
        });

        fieldEditor.setEnabled(true);
        fieldEditor.takeFocus();

    }

    private void resetTextField() {
        taskTable.takeFocus();
        fieldEditor.setEnabled(false);
        fieldEditor.setText("");
        editingFieldNameLabel.setText("");
        fieldEditor.setPreferredSize(new TerminalSize(0, 0));
    }

    private void refreshScreen() {
        TableModel<String> taskDataInfo = new TableModel<>();
        for (String fieldName : fieldNames) {
            taskDataInfo.addColumn(fieldName, null);
        }
        taskDataInfo.addRow(
                task.getTitle(),
                task.getDescription(),
                task.isCompleted() ? "X" : " ",
                String.valueOf(task.getPriority()),
                task.getDeadline() == null ? "-" : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()));
        taskTable.setTableModel(taskDataInfo);
    }

    public TaskEditor(Task task, int userRights, List<Task> tasks, TaskEditedListener listener) {
        this.task = task;
        setCloseWindowWithEscape(true);
        panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        this.tasks = tasks;
        this.listener = listener;
        panel.addComponent(new Label("Select column:"));
        panel.addComponent(new EmptySpace());

        taskTable = new Table<>(fieldNames);
        refreshScreen();

        //selects which part to edit (editing not implemented)
        /*for (int i = 0; i < taskDataInfo.getColumnCount(); i++) {
            int selectedI = i;
            actionListBox.addItem(String.valueOf(taskDataInfo.getColumnLabel(i)), () -> {
                if (listener != null) {
                    listener.partSelected(selectedI);
                }
            });
        }*/

        taskTable.setCellSelection(true);
        taskTable.setSelectAction(() -> {
            switch (taskTable.getSelectedColumn()) {
                case 0:
                    editText(taskTable.getSelectedColumn(), TaskEditor.this.task.getTitle(), false, null, newValue -> {
                        TaskEditor.this.task.setTitle(newValue);
                        refreshScreen();
                    });
                    break;
                case 1:
                    editText(taskTable.getSelectedColumn(), TaskEditor.this.task.getDescription(), true, null, newValue -> {
                        TaskEditor.this.task.setDescription(newValue);
                        refreshScreen();
                    });
                    break;
                case 2:
                    TaskEditor.this.task.setCompleted(!TaskEditor.this.task.isCompleted());
                    refreshScreen();
                    break;
                case 3:
                    editText(taskTable.getSelectedColumn(), String.valueOf(TaskEditor.this.task.getPriority()), false,
                            (interactable, keyStroke) ->
                                    keyStroke.getKeyType() != KeyType.Character
                                            || keyStroke.getCharacter().toString().matches("[\\d\\-]"),
                            newValue -> {
                                TaskEditor.this.task.setPriority(Integer.parseInt(newValue));
                                refreshScreen();
                            });
                    break;
                case 4:
                    editText(taskTable.getSelectedColumn(), task.getDeadline() == null ? DATETIME_FORMAT : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()), false,
                            (interactable, keyStroke) ->
                                    keyStroke.getKeyType() != KeyType.Character
                                            || keyStroke.getCharacter().toString().matches("[\\d.:]"), newValue -> {
                                try {
                                    Date newdate = new SimpleDateFormat(DATETIME_FORMAT).parse(newValue);
                                    TaskEditor.this.task.setDeadline(newdate);
                                    refreshScreen();
                                } catch (ParseException e) {
                                    System.out.println("Cannot parse date: " + newValue);
                                    throw new RuntimeException("Cannot parse date: " + newValue);
                                }
                            });
                    break;
                default:
            }
        });
        panel.addComponent(taskTable);
        panel.addComponent(new EmptySpace());

        editingFieldNameLabel = new Label("");
        fieldEditor = new TextBox(new TerminalSize(0, 0), TextBox.Style.MULTI_LINE);
        fieldEditor.setEnabled(false);
        panel.addComponent(editingFieldNameLabel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(fieldEditor);

        cancelBtn = new Button("Cancel");
        cancelBtn.addListener(button -> TaskEditor.this.close());

        saveBtn = new Button("Save");
        saveBtn.addListener(button -> {
            if (TaskEditor.this.listener != null)
                TaskEditor.this.listener.taskEdited(TaskEditor.this.task);
            TaskEditor.this.close();
        });

        panel.addComponent(new EmptySpace());
        panel.addComponent(saveBtn);
        panel.addComponent(cancelBtn);

        setComponent(panel);


    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
