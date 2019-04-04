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

public class TUI {
    private List<String> projects;
    private String displayUsername = "";
    private Client client;

    private boolean terminalRunning = false;
    private Terminal terminal;
    private Screen screen;
    private TextGraphics tg;

    public void stopTerminal() {
        terminalRunning = false;
    }

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

            SelectionMenu selectionMenu = new SelectionMenu(terminal, screen, tg, 20, projects, itemIndex -> {

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

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    /**
     * TODO: add username display somewhere on the TUI so the user can see who is currently logged in.
     * @param displayUsername
     */
    public void setDisplayUsername(String displayUsername) {
        this.displayUsername = displayUsername;
    }

    public TUI(Client client) {
        this.client = client;
        System.out.println("TUI constructor");


    }


    /**
     * Main method so we can test the UI separately.
     *
     * @param args
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