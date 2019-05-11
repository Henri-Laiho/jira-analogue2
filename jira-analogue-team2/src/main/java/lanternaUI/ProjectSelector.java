package lanternaUI;

import client.ClientMain;
import com.googlecode.lanterna.gui2.*;

import java.util.List;

class ProjectSelector extends BasicWindow {
    interface ProjectSelectedListener {
        void projectSelected(int projectIndex);
    }

    private ProjectSelectedListener listener;
    private ActionListBox actionListBox;
    private TextBox textBox;

    ProjectSelector(ProjectSelectedListener listener) {
        setCloseWindowWithEscape(true);
        this.listener = listener;

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

       /* Panel sidePanel = new Panel();
        sidePanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        sidePanel.setPosition(new TerminalPosition(5, 5));
        textBox = new TextBox();
        textBox.setText("User: " + ClientMain.getUser());
        sidePanel.addComponent(textBox);
*/
        //code for another panel above
        actionListBox = new ActionListBox();
        panel.addComponent(new Label("User: " + ClientMain.getUser() + "\nYou are a collaborator in these projects.\nSelect a project to open:"));
        panel.addComponent(actionListBox);

        setComponent(panel);

    }

    ProjectSelector() {
        this(null);
    }

    void setListener(ProjectSelectedListener listener) {
        this.listener = listener;
    }

    void setProjectList(List<String> projects) {
        actionListBox.clearItems();
        int i = 0;
        for (String project : projects) {
            int finalI = i;
            actionListBox.addItem(project, () -> {
                if (listener != null)
                    listener.projectSelected(finalI);
            });
            i++;
        }

        actionListBox.takeFocus();
    }
}
