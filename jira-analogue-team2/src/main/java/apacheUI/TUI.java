package apacheUI;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;

import java.io.IOException;

public class TUI{

        public static void main(String[] args) throws IOException {
            Terminal terminal = new DefaultVirtualTerminal();
            Screen screen = new TerminalScreen(terminal);
            TextGraphics tg = screen.newTextGraphics();

            screen.startScreen();
            tg.putString(9,9, "testing");
            screen.refresh();

        }
}