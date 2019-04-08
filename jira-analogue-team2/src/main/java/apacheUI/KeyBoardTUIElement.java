package apacheUI;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

/**
 * Abstract class for TUIElements that block and wait for keyboard input when run() is called
 */
abstract class KeyBoardTUIElement extends TUIElement {

    abstract boolean onKeyHit(KeyStroke keyStroke);
    //abstract boolean shouldContinue();

    private boolean running = false;

    KeyBoardTUIElement(Terminal terminal, Screen screen, TextGraphics tg) {
        super(terminal, screen, tg);
    }

    /**
     * Blocks and reads keyboard input until subclass onKeyHit() returns false.
     */
    @Override
    public void run() {
        super.run();
        running = true;
        while (running/* && shouldContinue()*/) {
            try {
                KeyStroke pressed = readInput();

                if (pressed != null) {
                    System.out.println(pressed);
                    running = onKeyHit(pressed);
                    refresh();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        running = false;
    }

}
