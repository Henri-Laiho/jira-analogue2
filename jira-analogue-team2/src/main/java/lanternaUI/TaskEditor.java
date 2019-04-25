package lanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import common.Task;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TaskEditor extends BasicWindow {
    interface TaskEditedListener {
        void partSelected(int taskIndex);
        //void taskEdited(Task task);
    }


    private TaskEditedListener listener = null;
    private List<Task> tasks;
    private Panel panel = new Panel();
    private List<TextBox> inputFields = new ArrayList<TextBox>();
    //private Table<String> taskTable;


    public TaskEditor(Task task, int userRights, List<Task> tasks, MultiWindowTextGUI gui, TaskEditedListener listener) {
        setCloseWindowWithEscape(true);
        panel = panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        this.tasks = tasks;
        this.listener = listener;
        panel.addComponent(new Label("Edit task"));
        panel.addComponent(new EmptySpace());

        TextBox title = add_property_to_tui("Title", task.getTitle(), 70, 1, null);
        TextBox priority = add_property_to_tui("Priority", String.valueOf(task.getPriority()), 2, 1, "[0-9]"); // really bad solution
        //TextBox deadline = add_property_to_tui("Deadline (BAD REPRESENTATION FIXME)", String.valueOf(task.getDeadline().getTime()), 70, 1, null);
        TextBox description = add_property_to_tui("Description", task.getDescription(), 70, 8, null);

        Button saveChanges = new Button("Save changes", () -> {
                System.out.println("Not saving changes.");
                String errors = "";
                int priorityVal = 0;
                //Date deadlineVal = task.getDeadline();

                try{
                    priorityVal = Integer.valueOf(priority.getText());
                } catch(NumberFormatException e){
                    errors += "Invalid priority. Must be \"[0-9]\".\n";
                }
                /*try{
                    deadlineVal = new Date(Long.valueOf(deadline.getText()));
                } catch(IllegalArgumentException e){
                    errors += "Invalid date, must be \"yyyy-[m]m-[d]d\".\n";
                }*/
                if (errors.equals("")){
                    task.setTitle(title.getText());
                    task.setPriority(priorityVal);
                    //task.setDeadline(deadlineVal);
                    task.setDescription(description.getText());
                    this.close();
                }
                else{
                    MessageDialog.showMessageDialog(gui, "Task can't be saved because of following errors", errors);
                }
            }
        );
        panel.addComponent(saveChanges);
        setComponent(panel);


    }

    /**
     * @param validatorRegex may be null
     */
    private TextBox add_property_to_tui(String name, String initialContent, int columns, int rows, String validatorRegex) {

        TextBox input = new TextBox(new TerminalSize(columns, rows), initialContent);
        if (validatorRegex != null) {
            input.setValidationPattern(Pattern.compile(validatorRegex));
        }
        panel.addComponent(new Label(name));
        panel.addComponent(input);
        panel.addComponent(new EmptySpace());
        return input;
    }

    public TaskEditor(Task task, int userRights, MultiWindowTextGUI gui, List<Task> tasks) {
        this(task, userRights, tasks, gui, null);
    }


    public void setListener(TaskEditedListener listener) {
        this.listener = listener;
    }
}
