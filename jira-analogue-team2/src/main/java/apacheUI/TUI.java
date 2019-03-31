package apacheUI;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class TUI {
    Terminal terminal;
    Screen screen;
    TextGraphics tg;

    public TUI(String[] args) throws IOException {
        System.out.println("test");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();

        terminal = dtf.createTerminal();
        screen = new TerminalScreen(terminal);
        tg = screen.newTextGraphics();

        screen.startScreen();
        tg.putString(9,9, "testing");
        screen.refresh();
    }

    public static void main(String[] args) throws IOException {
        new TUI(args);
    }
}