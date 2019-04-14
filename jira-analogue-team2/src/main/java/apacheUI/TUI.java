package apacheUI;

import client.Client;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import common.Project;
import common.Task;
import data.RawTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Text User Interface
 * <p>
 * <p>
 * <p>
 * Calling startTerminal() creates a terminal window and starts interacting
 * with the user. See startTerminal() javadoc for details.
 * <p>
 * Usage:
 * 1. Initialize a TUI object; TUI tui = new TUI(client);
 * 2. Set the list of projects; tui.setProjects(...)
 * 3. call startTerminal().
 */

public class TUI {
    private List<String> projects;
    private String displayUsername = "";
    private Client client;

    private boolean terminalRunning = false;
    private boolean editProject = false;
    private Terminal terminal;
    private Screen screen;
    private TextGraphics tg;
    private TextGraphics bg;

    /**
     * Set the terminal loops to break at the next iteration.
     */
    public void stopTerminal() {
        terminalRunning = false;
    }

    public void setEditProject(boolean editProject) {
        this.editProject = editProject;
    }

    /**
     * Creates a new terminal window; Blocks until user exits the TUI or the terminal window gets closed.
     * <p>
     * User will connect to server and log in using the Command Line Interface (CLI).
     * TODO: User can add a search option with -s. If the search option was specified, this method
     * TODO: will skip step 1. and select the project given in the search option.
     * <p>
     * 1. Ask the user (SelectionMenu with arrow keys and enter to select) which project should be opened.
     * TODO: 2. Show the list of tasks in the project and let the user select a task (SelectionMenu).
     * TODO: The first entry in the list of tasks should be "Create new task"; if selected start creating
     * TODO: a new task; ask the task name, description and if a new git branch should be created for the task.
     * TODO: 3. Show the details of the selected task; Let user edit the task (select an item to edit with
     * TODO: SelectionMenu and fill in new data with some input method).
     * <p>
     * User can press ESC to exit a SelectionMenu and select another project (or task).
     *
     * @param args command line arguments, TODO: if contains search option, autoimatically search and open the project (request the project from server).
     * @throws IOException if there is an i/o error
     */
    public void startTerminal(String[] args) throws IOException, InterruptedException {
        System.out.println("Opening terminal.");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();

        terminal = dtf.createTerminal();
        screen = new TerminalScreen(terminal);
        tg = screen.newTextGraphics();
        bg = screen.newTextGraphics();

        screen.startScreen();
        bg.setBackgroundColor(TextColor.ANSI.BLACK);
        tg.putString(6, 4, "welcome to the minjira text user interface");
        tg.putString(6, 5, "select project name (up and down arrow, hit enter to select):");
        tg.putString(6, 25, "hit ESC to exit");
        TUIBorders tuiBorders = new TUIBorders(screen, bg);
        tuiBorders.runBorder();
        screen.refresh();
        terminalRunning = true;
        int i = 0;
        boolean hit = false;

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        while (terminalRunning) {

            // Keep asking for a project to open
            ProjectSelector projectSelector = new ProjectSelector();
            projectSelector.setListener(new ProjectSelector.ProjectSelectedListener() {
                boolean editProject = true;
                int selectedIndex = -1;

                @Override
                public void projectSelected(int projectIndex) {
                    // open project projectIndex

                    try {
                        if (client.selectProject(projectIndex)) {
                            if (client.getUserRightsInProject() > 0) {

                                while (terminalRunning && editProject) {
                                    Project project = client.getOpenedProject();
                                    List<String> titles = new ArrayList<>();
                                    List<String> completed = new ArrayList<>();
                                    List<String> priorities = new ArrayList<>();
                                    List<String> deadlines = new ArrayList<>();
                                    project.getTasklist().forEach(task -> {
                                        titles.add(task.getTitle());
                                        completed.add(task.isCompleted() ? "Done" : "Not Done");
                                        priorities.add(String.valueOf(task.getPriority()));
                                        deadlines.add(task.getDeadline().toString());
                                    });

                                    ProjectEditor projectEditor = new ProjectEditor();
                                    projectEditor.setListener(new ProjectEditor.TaskSelectedListener() {
                                        @Override
                                        public void taskSelected(int taskIndex) {
                                            selectedIndex = taskIndex;
                                            Task task = project.getTasklist().get(taskIndex);

                                            projectEditor.close();
                                            TaskEditor taskEditor = new TaskEditor(task, client.getUserRightsInProject(), project.getTasklist(), task1 -> {
                                                try {
                                                    client.sendUpdateTask(task);
                                                } catch (IOException | InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });

                                            projectEditor.close();
                                            gui.addWindowAndWait(taskEditor);
                                        }

                                        @Override
                                        public void createTask() {
                                            Task task = new Task(new RawTask(project.getNewValidTaskId(), false, "Enter Title",
                                                    "Enter description", -1, client.getUserId(), null, System.currentTimeMillis(),
                                                    null, null, null));

                                            TaskEditor taskEditor = new TaskEditor(task, client.getUserRightsInProject(), project.getTasklist(), task1 -> {
                                                try {
                                                    client.sendCreateTask(task);
                                                } catch (IOException | InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });

                                            projectEditor.close();
                                            gui.addWindowAndWait(taskEditor);
                                        }
                                    });

                                    projectEditor.setTaskList(titles, priorities, deadlines, completed, selectedIndex);

                                    // stop reopening the project editor window when escape is pressed - set ui loop condition to false.
                                    projectEditor.addWindowListener(new WindowListenerAdapter() {
                                        @Override
                                        public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                                            if (keyStroke.getKeyType() == KeyType.Escape)
                                                editProject = false;
                                        }
                                    });
                                    projectSelector.close();
                                    gui.addWindowAndWait(projectEditor);
                                }

                                // projectUi(client.getOpenedProject(), client.getUserRightsInProject());
                            } else System.out.println("You dont have rights to view this project.");
                        } else System.out.println("Failed to open project.");
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            projectSelector.setProjectList(projects);

            gui.addWindowAndWait(projectSelector);

            /*SelectionMenu selectionMenu = new SelectionMenu(terminal, screen, tg, projects);
            int itemIndex = selectionMenu.runForSelectedItemIndex();

            if (itemIndex == -1) {
                stopTerminal();
                continue;
            }

            tg.putString(0, 7, "opening project...", SGR.BOLD);
            screen.refresh();

            // a useless delay to make the user think a project is being downloaded from the server
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tg.putString(0, 7, "                               ");

            // open project
            boolean canOpenProject;
            try {
                if (client.selectProject(itemIndex)) {
                    // TODO: create new SelectionMenu to select a task to see more details or select "create new task" option
                    if (client.getUserRightsInProject() > 0)
                        projectUi(client.getOpenedProject(), client.getUserRightsInProject());
                    else System.out.println("You dont have rights to view this project.");
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/


        }

        screen.refresh();
        screen.readInput();
        screen.stopScreen();

        terminal.close();
        System.exit(0);
    }

    /**
     * Lets the user edit a project: view tasks and create more.
     *
     * @param openedProject the project to display to the user.
     */
    private void projectUi(Project openedProject, int rights) {
        List<String> options = new ArrayList<>();
        options.add("Create task");

        // number of extra options before the task list like "create task"
        int extraoptions = options.size();

        openedProject.getTasklist().forEach(task -> options.add(task.getTitle()));

        SelectionMenu selectionMenu = new SelectionMenu(terminal, screen, tg, options);
        int itemIndex = selectionMenu.runForSelectedItemIndex();

        if (itemIndex == -1) {
            return;
        }

        if (itemIndex < extraoptions) {
            if (itemIndex == 0) {
                // create task
                editTaskUi(openedProject, null);

            }
        } else {
            editTaskUi(openedProject, openedProject.getTasklist().get(itemIndex - extraoptions));
        }
    }

    /**
     * Lets the user edit a project: view tasks and create more.
     *
     * @param task the task to be edited or null if a new task should be created.
     * @return true if task was edited or created.
     */
    private boolean editTaskUi(Project project, Task task) {
        boolean newtask = false;
        if (task == null) {
            newtask = true;
            task = new Task(new RawTask(project.getNewValidTaskId(), false, "Enter Title",
                    "Enter description", -1, client.getUserId(), null, System.currentTimeMillis(),
                    null, null, null));
        }

        // TODO: create new TUIElement type to conveniently edit the task data fields;
        // TODO: add here blocking UI to let the user edit the task

        try {
            if (newtask) client.sendCreateTask(task);
            else client.sendUpdateTask(task);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
            //return false;
        }
        return true;
    }

    /**
     * Update the list of projects
     *
     * @param projects the list of project names
     */
    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    /**
     * TODO: add username display somewhere on the TUI so the user can see who is currently logged in.
     *
     * @param displayUsername username that can be displayed to the user to indicate which user is logged in.
     */
    public void setDisplayUsername(String displayUsername) {
        this.displayUsername = displayUsername;
    }

    /**
     * Initialize the TUI object and remember a reference to the client object to call methods that
     * send requests to server when the user interacts with the user interface.
     *
     * @param client the client.
     */
    public TUI(Client client) {
        this.client = client;
        System.out.println("TUI constructor");
    }

    /**
     * Main method so we can test the UI separately.
     *
     * @param args unused command line arguments
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> projects = new ArrayList<>();
        projects.add("project jira");
        projects.add("onTime google");
        projects.add("elephant rush");
        projects.add("panda express");
        projects.add("moodle hack");
        projects.add("dong bong");
        projects.add("helper");
        projects.add("python for java programmers");

        TUI tui = new TUI(null);
        tui.setProjects(projects);
        tui.startTerminal(args);
    }
}