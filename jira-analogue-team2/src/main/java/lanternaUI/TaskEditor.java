package lanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import common.Project;
import common.Task;
import common.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static common.Constants.DATETIME_FORMAT;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void taskEdited(Task task);
    }

    interface TextFieldEditedListener {
        void textFieldEdited(String newValue);
    }

    interface ItemSelectedListener {
        void itemSelected(int itemIndex);
    }

    private static final String[] fieldNames = {"Title", "Description", "Is Done", "Priority", "Deadline", "Master Task"};

    private Panel panel;
    private TaskEditedListener listener;
    private Map<Long, Task> tasks;
    private List<String> taskTitles;
    private List<Long> taskIds;
    private Table<String> taskTable;
    private Label editingFieldNameLabel;
    private TextBox fieldEditor;
    private Button cancelBtn;
    private Button saveBtn;
    private Task task;

    private void selectItem(int fieldIndex, int initial, List<String> items, InputFilter filter, ItemSelectedListener listener) {
        String initialText;
        if (initial >= 0 && initial < items.size())
            initialText = items.get(initial);
        else
            initialText = "";
        fieldEditor.setText(initialText);
        fieldEditor.setPreferredSize(new TerminalSize(20, 1));

        fieldEditor.setInputFilter(new InputFilter() {
            //private String input = initialText;
            private int selected = initial;

            @Override
            public boolean onInput(Interactable interactable, KeyStroke keyStroke) {
                if (filter == null || filter.onInput(interactable, keyStroke)) {
                    String text = fieldEditor.getText();
                    if (keyStroke.getKeyType() == KeyType.Tab) {
                        int i = 0;
                        for (String s : items) {
                            if (s.startsWith(text)) {
                                fieldEditor.setText(s);
                                selected = i;
                                break;
                            }
                            i++;
                        }
                        return false;
                    } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
                        if (selected > 0)
                            selected--;
                        if (selected >= 0)
                            fieldEditor.setText(items.get(selected));
                        return false;
                    } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
                        if (selected < items.size() - 1)
                            selected++;
                        fieldEditor.setText(items.get(selected));
                        return false;
                    }
                    if (keyStroke.getKeyType() == KeyType.Escape) {
                        TaskEditor.this.resetTextField();
                        return false;
                    }
                    if (keyStroke.getKeyType() == KeyType.Enter && selected >= 0 && selected < items.size()) {
                        if (listener != null)
                            listener.itemSelected(selected);
                        TaskEditor.this.resetTextField();
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });

        fieldEditor.setEnabled(true);
        fieldEditor.takeFocus();
    }

    private void editText(int fieldIndex, String initial, boolean multiline, InputFilter filter, TextFieldEditedListener listener) {
        editText(fieldIndex, initial, multiline, filter, listener, null);
    }

    private void editText(int fieldIndex, String initial, boolean multiline, InputFilter filter, TextFieldEditedListener listener, String label) {

        if (label == null)
            editingFieldNameLabel.setText(fieldNames[fieldIndex] + ":");
        else
            editingFieldNameLabel.setText(label);

        fieldEditor.setText(initial);

        fieldEditor.setPreferredSize(new TerminalSize(30, multiline ? 10 : 1));

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
                task.getDeadline() == null ? "-" : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()),
                task.getMasterTask() == null ? "-" : task.getMasterTask().getTitle()
        );

        taskTable.setTableModel(taskDataInfo);

    }

    public TaskEditor(Task task, int userRights, Map<Long, Task> tasks, TaskEditedListener listener) {
        this.task = task;
        setCloseWindowWithEscape(true);
        panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        this.tasks = tasks;
        this.listener = listener;

        ArrayList<Map.Entry<Long, Task>> entries = new ArrayList<>(tasks.entrySet());
        entries.sort(Comparator.comparing(o -> o.getValue().getTitle()));

        taskTitles = new ArrayList<>(tasks.size());
        taskIds = new ArrayList<>(tasks.size());
        for (Map.Entry<Long, Task> task1 : entries) {
            if (task1.getKey() != task.getTaskId()) {
                taskTitles.add(task1.getValue().getTitle());
                taskIds.add(task1.getValue().getTaskId());
            }
        }

        panel.addComponent(new Label("Select column(CTRL+ENTER to confirm input):"));
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
                    editText(taskTable.getSelectedColumn(), TaskEditor.this.task.getTitle().equals("Enter Title") ? "" : TaskEditor.this.task.getTitle() , false, null, newValue -> {
                        TaskEditor.this.task.setTitle(newValue);
                        refreshScreen();
                    });
                    break;
                case 1:
                    editText(taskTable.getSelectedColumn(), TaskEditor.this.task.getDescription().equals("Enter description") ? "" : TaskEditor.this.task.getDescription(), true, null, newValue -> {
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
                    editText(taskTable.getSelectedColumn(),
                            task.getDeadline() == null ? " " : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()),
                            false,
                            (interactable, keyStroke) ->
                                    keyStroke.getKeyType() != KeyType.Character
                                            || keyStroke.getCharacter().toString().matches("[\\d.:]"),
                            newValue -> {
                                try {
                                    Date newdate = new SimpleDateFormat(DATETIME_FORMAT).parse(newValue);
                                    TaskEditor.this.task.setDeadline(newdate);
                                    refreshScreen();
                                } catch (ParseException e) {
                                    System.out.println("Cannot parse date: " + newValue);
                                    //throw new RuntimeException("Cannot parse date: " + newValue);
                                }
                            },
                            "Deadline: (" + DATETIME_FORMAT + ")");
                    break;
                case 5:
                    selectItem(taskTable.getSelectedColumn(),
                            TaskEditor.this.task.getMasterTask() == null ? -1 : taskIds.indexOf(TaskEditor.this.task.getMasterTask().getTaskId()),
                            taskTitles,
                            null,
                            result -> {
                                TaskEditor.this.task.setMasterTask(TaskEditor.this.tasks.get(taskIds.get(result)));
                                refreshScreen();
                            });
                    break;
                /*case 6:
                    editText(taskTable.getSelectedColumn(), "", true, null, newValue -> {
                        if (newValue.startsWith("www.github.com")) {
                            Project.setRepositoryUrl(newValue);
                            refreshScreen();
                        } else {
                            System.out.println("illegal repo link, must contain www.github.com");
                        }
                    });
                    break;*/
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
        panel.addComponent(cancelBtn);
        panel.addComponent(saveBtn);


        setComponent(panel);

    }

    public TaskEditor(Task task, int userRights, Map<Long, Task> tasks) {
        this(task, userRights, tasks, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
