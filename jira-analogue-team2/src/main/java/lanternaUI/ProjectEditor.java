package lanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;
import common.Project;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

class ProjectEditor extends BasicWindow {
    interface TaskSelectedListener {
        void taskSelected(int taskIndex);

        void createTask();

        void setRepoUrl(String url);

        String getRepoUrl();
    }

    private TaskSelectedListener listener;
    private ActionListBox actionListBox;
    private Table<String> taskData;
    private Panel panel;

    private Label editingFieldNameLabel;
    private TextBox fieldEditor;

    private void editRepoUrl(String initial, InputFilter filter) {
        editingFieldNameLabel.setText("Enter repository url:");
        fieldEditor.setText(initial == null ? "" : initial);
        fieldEditor.setPreferredSize(new TerminalSize(30, 1));

        fieldEditor.setInputFilter((interactable, keyStroke) -> {
            if (filter == null || filter.onInput(interactable, keyStroke)) {
                if (keyStroke.getKeyType() == KeyType.ArrowUp
                        || keyStroke.getKeyType() == KeyType.ArrowDown
                        || keyStroke.getKeyType() == KeyType.Tab)
                    return false;
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    resetTextField();
                    actionListBox.setSelectedIndex(actionListBox.getItemCount()-2);
                    return false;
                }
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    if (listener != null)
                        listener.setRepoUrl(fieldEditor.getText());
                    resetTextField();
                    actionListBox.setSelectedIndex(actionListBox.getItemCount()-2);
                    return false;
                }
                if (keyStroke.getKeyType() == KeyType.Character && keyStroke.isCtrlDown()){
                    if (keyStroke.getCharacter() == 'v') {
                        try {
                            fieldEditor.setText((String) Toolkit.getDefaultToolkit()
                                    .getSystemClipboard().getData(DataFlavor.stringFlavor));
                        } catch (UnsupportedFlavorException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            System.out.println("Cannot get url from clipboard.");
                            throw new RuntimeException(e);
                        }
                        return false;
                    }
                }
                return true;
            }
            return false;
        });

        fieldEditor.setEnabled(true);
        fieldEditor.takeFocus();
    }

    private void resetTextField() {
        actionListBox.takeFocus();
        fieldEditor.setEnabled(false);
        fieldEditor.setText("");
        editingFieldNameLabel.setText("");
        fieldEditor.setPreferredSize(new TerminalSize(0, 0));
    }

    ProjectEditor(TaskSelectedListener listener) {
        setCloseWindowWithEscape(true);
        this.listener = listener;

        panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        actionListBox = new ActionListBox();
        taskData = new Table<>("Is Done", "Priority", "Deadline");

        // make not focusable, cursor cannot go into the table
        taskData.setEnabled(false);

        panel.addComponent(new Label("Select a task or add a new task\n(or open project in github):"));
        panel.addComponent(new EmptySpace());

        panel.addComponent(actionListBox);
        panel.addComponent(taskData);

        editingFieldNameLabel = new Label("");
        fieldEditor = new TextBox(new TerminalSize(0, 0), TextBox.Style.SINGLE_LINE);
        fieldEditor.setEnabled(false);

        panel.addComponent(editingFieldNameLabel);
        panel.addComponent(fieldEditor);

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

        actionListBox.addItem("Edit repository url", () -> {
            if (listener != null) {
                editRepoUrl(listener.getRepoUrl(), null);
            }
        });

        if (listener != null && listener.getRepoUrl() != null) {
            actionListBox.addItem("Open repo in browser", () -> {
                if (listener != null) {
                    if (listener.getRepoUrl() == null) {
                        editRepoUrl(null, null);
                    } else {
                        try {
                            openWebpage(URI.create(listener.getRepoUrl()));
                        } catch (IOException | IllegalArgumentException e) {
                            System.out.println("Cannot open repository url.");
                            editRepoUrl(listener.getRepoUrl(), null);
                        }
                    }
                }
            });
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

        actionListBox.setSelectedIndex(selectedTask + extraOptionCount);
        actionListBox.takeFocus();
    }

    void setTaskList(List<String> taskTitles, List<String> priorities, List<String> deadlines, List<String> completed) {
        setTaskList(taskTitles, priorities, deadlines, completed, 0);
    }

    //did not create these classes myself, but pretty easy stuff
    public static boolean openWebpage(URI uri) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri);
            return true;
        }
        return false;
    }

    public static boolean openWebpage(URL url) throws IOException, URISyntaxException {
        return openWebpage(url.toURI());
    }


}
