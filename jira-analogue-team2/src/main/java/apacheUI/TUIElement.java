package apacheUI;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * Abstract class for Text User Interface elements like the SelectionMenu and other widgets used in the TUI
 * that should be reusable.
 */
abstract class TUIElement implements Runnable {
    Terminal terminal;
    Screen screen;
    TextGraphics tg;
    private TextColor foreground;
    private TextColor background;
    private TUIElement parent;

    // the position of this element on the text graphics screen.
    int row = 0;
    int column = 0;

    // TUI elements can't be shrinked to fit in a smaller area.
    /*int width = 0;
    int heigth = 0;*/

    /**
     * Returns the absolute row coordinate of given relative coordinate in the TextGraphics object
     *
     * All TUI elements should draw elements using these methods without considering their
     * location on the screen.
     *
     * @param relativeRow the relative coordinate.
     * @return the absolute coordinate.
     */
    int getAbsoluteRow(int relativeRow) {
        if (parent == null)
            return row+relativeRow;
        return parent.getAbsoluteRow(row+relativeRow);
    }

    /**
     * Returns the absolute column coordinate of given relative coordinate in the TextGraphics object
     *
     * All TUI elements should draw elements using these methods without considering their
     * location on the screen.
     *
     * @param relativeColumn the relative coordinate.
     * @return the absolute coordinate.
     */
    int getAbsoluteColumn(int relativeColumn) {
        if (parent == null)
            return column+relativeColumn;
        return parent.getAbsoluteColumn(column+relativeColumn);
    }

    /**
     * Constructor without a parent TUIElement
     * @param terminal Terminal
     * @param screen Screen
     * @param tg TextGraphics
     */
    TUIElement(Terminal terminal, Screen screen, TextGraphics tg) {
        this.terminal = terminal;
        this.screen = screen;
        this.tg = tg;
        this.parent = null;
    }

    /**
     * Constructor with a parent TUIElement
     * @param terminal Terminal
     * @param screen Screen
     * @param tg TextGraphics
     */
    TUIElement(Terminal terminal, Screen screen, TextGraphics tg, TUIElement parent) {
        this.terminal = terminal;
        this.screen = screen;
        this.tg = tg;
        this.parent = parent;
    }

    public void setBackground(TextColor background) {
        this.background = background;
    }

    public void setForeground(TextColor foreground) {
        this.foreground = foreground;
    }

    /**
     * Draws the TUIElement.
     * Blocking TUIElements should be drawn last
     */
    @Override
    public void run() {
        if (foreground != null)
            tg.setForegroundColor(foreground);
        if (background != null)
            tg.setBackgroundColor(background);
    }
}
