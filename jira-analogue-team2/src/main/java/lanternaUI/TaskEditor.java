package lanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
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
    private List<Task> tasks;
    private List<String> taskTitles;
    private Table<String> taskTable;
    private Label editingFieldNameLabel;
    private TextBox fieldEditor;
    private Button cancelBtn;
    private Button saveBtn;
    private Task task;

    private void selectItem(int fieldIndex, String initial, List<String> items, InputFilter filter, ItemSelectedListener listener) {
        Collections.sort(items);
        editingFieldNameLabel.setText(fieldNames[fieldIndex]);
        fieldEditor.setText(initial);
        fieldEditor.setPreferredSize(new TerminalSize(20, 1));

        fieldEditor.setInputFilter(new InputFilter() {
            private String input = initial;
            private int selected = items.indexOf(initial);
            @Override
            public boolean onInput(Interactable interactable, KeyStroke keyStroke) {
                if (filter == null || filter.onInput(interactable, keyStroke)) {
                    String text = fieldEditor.getText();
                    if (keyStroke.getKeyType() == KeyType.Tab) {
                        int i = 0;
                        for (String s : items) {
                            if (s.startsWith(input)) {
                                input = fieldEditor.getText();
                                fieldEditor.setText(s);
                                selected = i;
                                break;
                            }
                            i++;
                        }
                        return false;
                    } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
                        for (int i = selected-1; i >= 0; i--) {
                            if (items.get(i).startsWith(input)) {
                                fieldEditor.setText(items.get(i));
                                selected = i;
                                break;
                            }
                        }
                        return false;
                    } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
                        if (selected >= 0) {
                            for (int i = selected+1; i < items.size(); i++) {
                                if (items.get(i).startsWith(input)) {
                                    fieldEditor.setText(items.get(i));
                                    selected = i;
                                    break;
                                }
                            }
                        } else {
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).startsWith(input)) {
                                    input = fieldEditor.getText();
                                    fieldEditor.setText(items.get(i));
                                    selected = i;
                                    break;
                                }
                            }
                        }
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
                    input = fieldEditor.getText();
                    return true;
                }
                return false;
            }
        });

        fieldEditor.setEnabled(true);
        fieldEditor.takeFocus();
    }

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
                task.getDeadline() == null ? "-" : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()),
                task.getMasterTask() == null ? "-" : task.getMasterTask().getTitle());
        taskTable.setTableModel(taskDataInfo);
    }

    public TaskEditor(Task task, int userRights, List<Task> tasks, TaskEditedListener listener) {
        this.task = task;
        setCloseWindowWithEscape(true);
        panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        this.tasks = tasks;
        this.listener = listener;

        taskTitles = new ArrayList<>(tasks.size());
        for (Task task1 : tasks) {
            taskTitles.add(task1.getTitle());
        }

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
                    editText(taskTable.getSelectedColumn(),
                            task.getDeadline() == null ? DATETIME_FORMAT : new SimpleDateFormat(DATETIME_FORMAT).format(task.getDeadline()),
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
                            });
                    break;
                case 5:
                    selectItem(taskTable.getSelectedColumn(),
                            TaskEditor.this.task.getMasterTask() == null ? "" : TaskEditor.this.task.getMasterTask().getTitle(),
                            taskTitles,
                            null,
                            result -> {
                                TaskEditor.this.task.setMasterTask(TaskEditor.this.tasks.get(result));
                                refreshScreen();
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
        panel.addComponent(cancelBtn);
        panel.addComponent(saveBtn);

        setComponent(panel);


    }

    public TaskEditor(Task task, int userRights, List<Task> tasks) {
        this(task, userRights, tasks, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
