package apacheUI;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TUI {

    Terminal terminal;
    Screen screen;
    TextGraphics tg;

    public TUI(String[] args) throws IOException, InterruptedException {
        List<String> projects = new ArrayList<>();
        projects.add("project jira");
        projects.add("onTime google");
        projects.add("elephant rush");
        projects.add("panda express");
        projects.add("moodle hack");
        projects.add("dong bong");
        projects.add("helper");
        projects.add("python for java programmers");
        System.out.println("test");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();

        terminal = dtf.createTerminal();
        screen = new TerminalScreen(terminal);
        tg = screen.newTextGraphics();

        screen.startScreen();
        tg.putString(0, 1, "welcome to the minjira text user interface");
        tg.putString(0, 2, "select project name (up and down arrow, hit enter to select):");
        tg.putString(0, 22,"hit ESC to exit");
        screen.refresh();
        boolean running = true;
        int i = 0;
        boolean hit = false;

        while (running) {

            KeyStroke pressed = terminal.pollInput();

            if (pressed != null) {
                System.out.println(pressed);
                switch ((pressed.getKeyType())) {
                    case Escape:
                        running = false;
                        break;

                    case ArrowUp:
                        if (i < projects.size() - 1) {
                            i += 1;
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }
                        else {
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }

                    case ArrowDown:
                        if (i > 0) {
                            i -= 1;
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }
                        else {
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }
                    case Enter:
                        hit = true;
                        tg.putString(0, 4, "                                                                                      ");
                        tg.putString(0, 4, "you have selected: " + projects.get(i), SGR.ITALIC);
                        tg.putString(0, 5,"hit rightarrow to open");
                        break;
                    case ArrowRight:
                        if (hit) {
                            tg.putString(0, 7, "opening project...", SGR.BOLD);
                            screen.refresh();

                            TimeUnit.SECONDS.sleep(2);
                            tg.putString(0, 7, "                               ");
                            //insert actual opening of project
                            //method here
                            //
                            //method here
                            break;
                        }

                }
                screen.refresh();
            }

        }
        screen.refresh();
        screen.readInput();
        screen.stopScreen();

        System.exit(0);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new TUI(args);
    }
}