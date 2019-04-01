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

public class TUI {

    Terminal terminal;
    Screen screen;
    TextGraphics tg;

    public TUI(String[] args) throws IOException {
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
        boolean running = true;
        int i = 0;

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
                            //small glitch:
                            //reaching the last element makes it go back to the second to last
                            //also instantly assumes that enter is hit
                            i += 1;
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }

                    case ArrowDown:
                        if (i > 0) {
                            i += -1;
                            tg.putString(0, 3, "                            ");
                            tg.putString(0, 3, projects.get(i), SGR.BOLD);
                            break;
                        }
                    case Enter:
                        tg.putString(0, 4, "                                                                                      ");
                        tg.putString(0,4, "you have selected: " + projects.get(i));
                        //insert opening project method/new screen method
                        //
                        //here
                        break;
                }
                screen.refresh();
            }

        }
        screen.refresh();
        screen.readInput();
        screen.stopScreen();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        new TUI(args);
    }
}