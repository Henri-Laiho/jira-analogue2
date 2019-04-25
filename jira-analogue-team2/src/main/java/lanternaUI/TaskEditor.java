package lanternaUI;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import common.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

import static common.Constants.DATETIME_FORMAT;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        //void partSelected(int taskIndex);
        void taskEdited(Task task);
    }

    interface TextFieldEditedListener {
        void textFieldEdited(String newValue);
    }

    private static final String[] fieldNames = {"Title", "Description", "Is Done", "Priority", "Deadline"};

    private Panel panel;
    private TaskEditedListener listener = null;
    private ActionListBox actionListBox;
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

        fieldEditor.setInputFilter((interactable, keyStroke) -> {
            if (filter == null || filter.onInput(interactable, keyStroke)) {
                if (keyStroke.getKeyType() == KeyType.ArrowUp
                        || keyStroke.getKeyType() == KeyType.ArrowDown
                        || keyStroke.getKeyType() == KeyType.Tab)
                    return false;
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    taskTable.takeFocus();
                    fieldEditor.setEnabled(false);
                    fieldEditor.setText("");
                    return false;
                }
                if ((multiline || keyStroke.isCtrlDown()) && keyStroke.getKeyType() == KeyType.Enter) {
                    listener.textFieldEdited(fieldEditor.getText());
                    taskTable.takeFocus();
                    fieldEditor.setEnabled(false);
                    fieldEditor.setText("");
                    return false;
                }
                return true;
            }
            return false;
        });

        fieldEditor.setEnabled(true);
        fieldEditor.takeFocus();

    }

    private void refreshScreen() {
        TableModel<String> taskDataInfo = new TableModel<>();
        for (String fieldName : fieldNames) {
            taskDataInfo.addColumn(fieldName, null);
        }
        taskDataInfo.addRow(task.getTitle(), task.getDescription(), task.isCompleted() ? "X" : " ", String.valueOf(task.getPriority()), new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()));
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
                    editText(taskTable.getSelectedColumn(), new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()), false,
                            (interactable, keyStroke) ->
                                    keyStroke.getKeyType() != KeyType.Character
                                            || keyStroke.getCharacter().toString().matches("[\\d.:]"), new TextFieldEditedListener() {
                                @Override
                                public void textFieldEdited(String newValue) {
                                    try {
                                        Date newdate = new SimpleDateFormat(DATETIME_FORMAT).parse(newValue);
                                        TaskEditor.this.task.setDeadline(newdate);
                                        refreshScreen();
                                    } catch (ParseException e) {
                                        System.out.println("Cannot parse date: " + newValue);
                                        throw new RuntimeException("Cannot parse date: " + newValue);
                                    }
                                }
                            });
                    break;
                default:
            }
        });
        panel.addComponent(taskTable);
        panel.addComponent(new EmptySpace());

        editingFieldNameLabel = new Label("");
        fieldEditor = new TextBox();
        fieldEditor.setEnabled(false);
        panel.addComponent(editingFieldNameLabel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(fieldEditor);

        setComponent(panel);


    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
