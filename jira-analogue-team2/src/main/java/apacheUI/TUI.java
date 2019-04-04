package apacheUI;

import client.Client;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Text User Interface
 *
 *
 *
 * Calling startTerminal() creates a terminal window and starts interacting
 * with the user. See startTerminal() javadoc for details.
 *
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
    private Terminal terminal;
    private Screen screen;
    private TextGraphics tg;

    /**
     * Set the terminal loops to break at the next iteration.
     */
    public void stopTerminal() {
        terminalRunning = false;
    }

    /**
     * Creates a new terminal window; Blocks until user exits the TUI or the terminal window gets closed.
     *
     * User will connect to server and log in using the Command Line Interface (CLI).
     * TODO: User can add a search option with -s. If the search option was specified, this method
     * TODO: will skip step 1. and select the project given in the search option.
     *
     * 1. Ask the user (SelectionMenu with arrow keys and enter to select) which project should be opened.
     * TODO: 2. Show the list of tasks in the project and let the user select a task (SelectionMenu).
     * TODO: The first entry in the list of tasks should be "Create new task"; if selected start creating
     * TODO: a new task; ask the task name, description and if a new git branch should be created for the task.
     * TODO: 3. Show the details of the selected task; Let user edit the task (select an item to edit with
     * TODO: SelectionMenu and fill in new data with some input method).
     *
     * User can press ESC to exit a SelectionMenu and select another project (or task).
     *
     * @param args command line arguments, TODO: if contains search option, autoimatically search and open the project (request the project from server).
     * @throws IOException if there is an i/o error
     */
    public void startTerminal(String[] args) throws IOException {
        System.out.println("Opening terminal.");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();

        terminal = dtf.createTerminal();
        screen = new TerminalScreen(terminal);
        tg = screen.newTextGraphics();

        screen.startScreen();
        tg.putString(0, 1, "welcome to the minjira text user interface");
        tg.putString(0, 2, "select project name (up and down arrow, hit enter to select):");
        tg.putString(0, 22, "hit ESC to exit");
        screen.refresh();
        terminalRunning = true;
        int i = 0;
        boolean hit = false;

        while (terminalRunning) {

            // Keep asking for a project to open

            SelectionMenu selectionMenu = new SelectionMenu(terminal, screen, tg, projects, itemIndex -> {

                if (itemIndex == -1) {
                    stopTerminal();
                    return false;
                }

                tg.putString(0, 7, "opening project...", SGR.BOLD);
                try {
                    screen.refresh();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                tg.putString(0, 7, "                               ");

                // open project
                boolean canOpenProject;
                try {
                    canOpenProject = client.selectProject(itemIndex);
                    // TODO: create new SelectionMenu to select a task to see more details or select "create new task" option
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //String project = projects.get(itemIndex);

                return !canOpenProject;
            });

            selectionMenu.run();

        }

        screen.refresh();
        screen.readInput();
        screen.stopScreen();

        terminal.close();
        System.exit(0);
    }

    /**
     * Update the list of projects
     * @param projects the list of project names
     */
    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    /**
     * TODO: add username display somewhere on the TUI so the user can see who is currently logged in.
     * @param displayUsername username that can be displayed to the user to indicate which user is logged in.
     */
    public void setDisplayUsername(String displayUsername) {
        this.displayUsername = displayUsername;
    }

    /**
     * Initialize the TUI object and remember a reference to the client object to call methods that
     * send requests to server when the user interacts with the user interface.
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