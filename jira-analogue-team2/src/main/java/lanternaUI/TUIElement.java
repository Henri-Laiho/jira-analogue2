package lanternaUI;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

/**
 * Abstract class for Text User Interface elements like the SelectionMenu and other widgets used in the TUI
 * that should be reusable.
 */
abstract class TUIElement implements Runnable {
    private Terminal terminal;
    private Screen screen;
    TextGraphics tg;
    TextGraphics bg;
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
     * <p>
     * All TUI elements should draw elements using these methods without considering their
     * location on the screen.
     *
     * @param relativeRow the relative coordinate.
     * @return the absolute coordinate.
     */
    int getAbsoluteRow(int relativeRow) {
        if (parent == null)
            return row + relativeRow;
        return parent.getAbsoluteRow(row + relativeRow);
    }

    /**
     * Returns the absolute column coordinate of given relative coordinate in the TextGraphics object
     * <p>
     * All TUI elements should draw elements using these methods without considering their
     * location on the screen.
     *
     * @param relativeColumn the relative coordinate.
     * @return the absolute coordinate.
     */
    int getAbsoluteColumn(int relativeColumn) {
        if (parent == null)
            return column + relativeColumn;
        return parent.getAbsoluteColumn(column + relativeColumn);
    }

    String nChars(int n, char c) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    void setPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    void refresh() throws IOException {
        screen.refresh();
    }

    KeyStroke readInput() throws IOException {
        return terminal.readInput();
    }

    void putString(int column, int row, String string) {
        tg.putString(getAbsoluteColumn(column), getAbsoluteRow(row), string);
    }

    void setBackgroundColor(TextColor color) {
        tg.setBackgroundColor(color);
    }

    void setForegroundColor(TextColor color) {
        tg.setForegroundColor(color);
    }

    void putCursor(int column, int row) throws IOException {
        screen.setCursorPosition(new TerminalPosition(getAbsoluteColumn(column), getAbsoluteRow(row)));
    }

    /**
     * Constructor without a parent TUIElement
     *
     * @param terminal Terminal
     * @param screen   Screen
     * @param tg       TextGraphics
     */
    TUIElement(Terminal terminal, Screen screen, TextGraphics tg) {
        this.terminal = terminal;
        this.screen = screen;
        this.tg = tg;
        this.parent = null;
    }

    TUIElement(Screen screen, TextGraphics bg) {
        this.screen = screen;
        this.bg = bg;
        this.parent = null;
    }

    /**
     * Constructor with a parent TUIElement
     *
     * @param terminal Terminal
     * @param screen   Screen
     * @param tg       TextGraphics
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
