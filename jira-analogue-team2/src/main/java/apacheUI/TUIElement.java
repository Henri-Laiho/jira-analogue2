package apacheUI;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

/**
 *
 */
abstract class TUIElement implements Runnable {
    Terminal terminal;
    Screen screen;
    TextGraphics tg;
    private TextColor foreground;
    private TextColor background;


    TUIElement(Terminal terminal, Screen screen, TextGraphics tg) {
        this.terminal = terminal;
        this.screen = screen;
        this.tg = tg;
        tg.putString(1, 2, "test");
    }

    public void setBackground(TextColor background) {
        this.background = background;
    }

    public void setForeground(TextColor foreground) {
        this.foreground = foreground;
    }

    @Override
    public void run() {
        if (foreground != null)
            tg.setForegroundColor(foreground);
        if (background != null)
            tg.setBackgroundColor(background);
    }
}
