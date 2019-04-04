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
    //private int pollDelayMS = 20;

    KeyBoardTUIElement(Terminal terminal, Screen screen, TextGraphics tg) {
        super(terminal, screen, tg);
    }

    /*KeyBoardTUIElement(Terminal terminal, Screen screen, TextGraphics tg, int pollDelayMS) {
        super(terminal, screen, tg);
        setPollDelayMS(pollDelayMS);
    }

    void setPollDelayMS(int pollDelayMS) {
        if (pollDelayMS > 0)
            this.pollDelayMS = pollDelayMS;
    }*/

    /**
     * Blocks and reads keyboard input until subclass onKeyHit() returns false.
     */
    @Override
    public void run() {
        super.run();
        running = true;
        while (running/* && shouldContinue()*/) {
            try {
                KeyStroke pressed = terminal.readInput();

                if (pressed != null) {
                    System.out.println(pressed);
                    running = onKeyHit(pressed);
                    screen.refresh();
                }
                /*else {
                    Thread.sleep(pollDelayMS);
                }*/
            } catch (IOException e) {
                throw new RuntimeException(e);
            } /*catch (InterruptedException e) {
                throw new RuntimeException(e);
                //running = false;
                //return;
            }*/
        }
        running = false;
    }
}
