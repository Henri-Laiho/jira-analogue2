package lanternaUI;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextEdit extends KeyBoardTUIElement {

    interface TextEditListener {
        /**
         * Called if user pressed enter to finish the text.
         *
         * @param text text entered by user.
         * @return false if TextEdit should be closed, true if user should enter more text.
         */
        boolean onTextEntered(List<String> text);
    }

    private TextEditListener listener = null;
    private List<StringBuilder> text = new ArrayList<>();
    private List<String> enteredText = null;
    private int cursorCol = 0;
    private int cursorRow = 0;

    private int view_column = 0;
    private int view_row = 0;

    private int max_visible_rows = 1;
    private int max_visible_columns = 1;
    private boolean single_line;
    private boolean insertOn = false;

    private int selectionStartRow;
    private int selectionStartCol;
    private int selectionEndRow;
    private int selectionEndCol;

    TextEdit(Terminal terminal, Screen screen, TextGraphics tg, int rows, int columns, boolean single_line) {
        super(terminal, screen, tg);
        text.add(new StringBuilder());
        if (rows > 1) max_visible_rows = rows;
        if (columns > 1) max_visible_columns = columns;
        this.single_line = single_line;
        deselect();
    }

    void setTextEditListener(TextEditListener listener) {
        this.listener = listener;
    }

    private boolean isSelected() {
        return selectionStartRow > -1 && selectionEndRow > -1;
    }

    private void deselect() {
        selectionStartRow = -1;
        selectionStartCol = -1;
        selectionEndRow = -1;
        selectionEndCol = -1;
    }

    private void select() {
        selectionStartRow = cursorRow;
        selectionStartCol = cursorCol;
        selectionEndRow = cursorRow;
        selectionEndCol = cursorCol;
    }

    private void selectEnd() {
        if (isSelected()) {
            selectionEndRow = cursorRow;
            selectionEndCol = cursorCol;
        } else select();
    }

    private void selectStart() {
        if (isSelected()) {
            selectionStartRow = cursorRow;
            selectionStartCol = cursorCol;
        } else select();
    }

    private void delete(boolean backspace) {
        StringBuilder row = text.get(cursorRow);
        if (isSelected()) {
            row = text.get(selectionStartRow);
            row.delete(selectionStartCol, row.length());
            for (int i = selectionStartRow + 1; i < selectionEndRow - 1; selectionEndRow--) {
                text.remove(i);
            }
            row = text.get(selectionEndRow);
            row.delete(0, selectionEndCol);

            cursorCol = selectionStartCol;
            decreaseColumn(0);
            increaseColumn(0);
            cursorRow = selectionStartRow;
            decreaseRow(0);
            increaseRow(0);
            deselect();
        } else if (backspace) {
            if (cursorCol > 0) {
                row.deleteCharAt(cursorCol - 1);
                decreaseColumn(1);
            } else if (cursorRow > 0) {

                cursorCol = text.get(cursorRow - 1).length();

                text.get(cursorRow - 1).append(row);
                text.remove(cursorRow);
                decreaseRow(1);

                decreaseColumn(0);
                increaseColumn(0);
            }
        } else {
            if (cursorCol < row.length()) {
                row.deleteCharAt(cursorCol);
            } else if (text.size() > cursorRow + 1) {
                row.append(text.get(cursorRow + 1));
                text.remove(cursorRow + 1);
            }
        }
    }

    private void insert(String s) {
        if (isSelected())
            delete(true);
        String[] strings = s.split("\n");
        StringBuilder row = text.get(cursorRow);
        row.append(strings[0]);

        for (int i = 1; i < strings.length - 1; i++) {
            text.add(cursorRow + i, new StringBuilder(strings[i]));
        }
        if (strings.length > 1) {
            row = text.get(cursorRow + strings.length - 1);
            row.insert(0, strings[strings.length - 1]);
        }
    }

    private void insert(char c) {
        if (isSelected())
            delete(true);
        StringBuilder row = text.get(cursorRow);
        if (c == '\n') {
            if (cursorCol == row.length()) {
                text.add(cursorRow + 1, new StringBuilder());
            } else {
                text.add(cursorRow + 1, new StringBuilder(row.subSequence(cursorCol, row.length())));
                row.delete(cursorCol, row.length());
            }
            decreaseColumn(cursorCol);
            increaseRow(1);
        } else {
            if (cursorCol == row.length() - 1) {
                row.append(c);
            } else if (insertOn) {
                row.setCharAt(cursorCol, c);
            } else {
                row.insert(cursorCol, c);
            }
            increaseColumn(1);
        }
    }

    private void increaseColumn(int n) {
        cursorCol += n;
        if (cursorCol > text.get(cursorRow).length())
            cursorCol = text.get(cursorRow).length();
        if (cursorCol > view_column + max_visible_columns)
            view_column = cursorCol - max_visible_columns;
    }

    private void decreaseColumn(int n) {
        cursorCol -= n;
        if (cursorCol < 0)
            cursorCol = 0;
        if (cursorCol < view_column)
            view_column = cursorCol;
    }

    private void increaseRow(int n) {
        cursorRow += n;
        if (cursorRow >= text.size())
            cursorRow = text.size() - 1;
        if (cursorRow >= view_row + max_visible_rows)
            view_row = cursorRow - max_visible_rows + 1;

        if (cursorCol >= text.get(cursorRow).length())
            decreaseColumn(cursorCol - text.get(cursorRow).length());
    }

    private void decreaseRow(int n) {
        cursorRow -= n;
        if (cursorRow < 0)
            cursorRow = 0;
        if (cursorRow < view_row)
            view_row = cursorRow;

        if (cursorCol >= text.get(cursorRow).length())
            decreaseColumn(cursorCol - text.get(cursorRow).length());
    }

    private void buildText() {
        enteredText = new ArrayList<>();
        for (StringBuilder stringBuilder : text) {
            enteredText.add(stringBuilder.toString());
        }
    }

    @Override
    void setPosition(int row, int column) {
        super.setPosition(row, column);
        try {
            draw();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSelection() {
        StringBuilder sb = new StringBuilder();
        StringBuilder row = text.get(selectionStartRow);
        sb.append(row.subSequence(selectionStartCol, selectionEndRow == selectionStartRow ? selectionEndCol : row.length() - 1));
        for (int i = selectionStartRow + 1; i < selectionEndRow - 1; i++) {
            sb.append('\n');
            sb.append(text.get(i));
        }
        if (selectionEndRow != selectionStartRow) {
            row = text.get(selectionEndRow);
            sb.append('\n');
            sb.append(row.subSequence(0, selectionEndCol));
        }
        return sb.toString();
    }

    private void draw() throws IOException {
        putCursor(cursorCol - view_column, cursorRow - view_row);
        for (int i = 0; i < max_visible_rows; i++) {
            if (i + view_row < text.size()) {
                StringBuilder row = text.get(i + view_row);
                // selection colors
                /*if (selectionStartRow == i + view_row && selectionEndRow == i + view_row) {
                    int start = 0;
                    int end = selectionStartCol;
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));

                    setBackgroundColor(TextColor.ANSI.BLUE);
                    start = selectionStartCol;
                    end = selectionEndCol;
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));

                    setBackgroundColor(TextColor.ANSI.BLACK);
                    start = selectionEndCol;
                    end = row.length();
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));
                } else if (selectionStartRow == i + view_row) {
                    int start = 0;
                    int end = selectionStartCol;
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));

                    setBackgroundColor(TextColor.ANSI.BLUE);
                    start = selectionStartCol;
                    end = row.length();
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));

                } else if (selectionEndRow == i + view_row) {
                    setBackgroundColor(TextColor.ANSI.BLUE);
                    int start = 0;
                    int end = selectionEndCol;
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));

                    setBackgroundColor(TextColor.ANSI.BLACK);
                    start = selectionEndCol;
                    end = row.length();
                    putString(start, i,
                            row.substring(Math.min(view_column, end), Math.min(end, view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - end), ' '));
                } else*/
                {
                    putString(0, i,
                            row.substring(Math.min(view_column, row.length()), Math.min(row.length(), view_column + max_visible_columns))
                                    + nChars(Math.max(0, view_column + max_visible_columns - row.length()), ' '));
                }
            } else {
                putString(0, i, nChars(max_visible_columns, ' '));
            }
        }
    }

    @Override
    boolean onKeyHit(KeyStroke keyStroke) {
        switch ((keyStroke.getKeyType())) {
            case Escape:
                enteredText = null;
                if (listener != null) listener.onTextEntered(null);
                return false;
            case Enter:
                if (single_line || keyStroke.isCtrlDown()) {
                    buildText();
                    if (listener != null) listener.onTextEntered(enteredText);
                    return false;
                } else {
                    insert('\n');
                }
                break;
            case EOF:
                enteredText = null;
                if (listener != null) listener.onTextEntered(null);
                return false;
            case ArrowUp:
                decreaseRow(1);

                if (keyStroke.isShiftDown()) selectStart();
                else deselect();
                break;
            case ArrowDown:
                increaseRow(1);

                if (keyStroke.isShiftDown()) selectEnd();
                else deselect();
                break;
            case ArrowRight:
                increaseColumn(1);

                if (keyStroke.isShiftDown()) selectEnd();
                else deselect();
                break;
            case ArrowLeft:
                decreaseColumn(1);

                if (keyStroke.isShiftDown()) selectStart();
                else deselect();
                break;
            case Backspace:
                delete(true);
                break;
            case Insert:
                insertOn = !insertOn;
                break;
            case Delete:
                delete(false);
                break;
            case Character:
                if (keyStroke.getCharacter() != null) {
                    char c = keyStroke.getCharacter();
                    if (keyStroke.isCtrlDown()) {
                        if (c == 'c') {
                            StringSelection selection = new StringSelection(getSelection());
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(selection, selection);
                        } else if (c == 'v') {
                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Clipboard clipboard = toolkit.getSystemClipboard();
                            try {
                                String result = (String) clipboard.getData(DataFlavor.stringFlavor);
                                insert(result);

                            } catch (UnsupportedFlavorException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        insert(c);
                    }
                }
                break;
            case Home:
                decreaseColumn(cursorCol);
                if (keyStroke.isShiftDown()) selectStart();
                else deselect();
                break;
            case End:
                increaseColumn(text.get(cursorRow).length() - cursorCol);
                if (keyStroke.isShiftDown()) selectEnd();
                else deselect();
                break;
            case PageUp:
                if (keyStroke.isCtrlDown()) {
                    decreaseRow(cursorRow);
                } else {
                    decreaseRow(max_visible_rows);
                }
                if (keyStroke.isShiftDown()) selectStart();
                else deselect();
                break;
            case PageDown:
                if (keyStroke.isCtrlDown()) {
                    increaseRow(text.size() - 1 - cursorRow);
                } else {
                    increaseRow(max_visible_rows);
                }
                if (keyStroke.isShiftDown()) selectEnd();
                else deselect();
                break;
            default:
                if (keyStroke.getCharacter() != null) {
                    char c = keyStroke.getCharacter();
                    insert(c);
                }
                break;
        }
        try {
            draw();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public List<String> runForEnteredText() {
        super.run();
        return enteredText;
    }
    /*public static void main(String[] args) throws IOException {
        System.out.println("Opening terminal.");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();


        Terminal terminal = dtf.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        TextGraphics tg = screen.newTextGraphics();

        screen.startScreen();
        tg.putString(0, 1, "welcome to the minjira text user interface");
        tg.putString(0, 22, "hit ESC to exit");
        screen.refresh();

        TextEdit textEdit = new TextEdit(terminal, screen, tg, 7, 32, false);
        textEdit.setPosition(2, 1);
        System.out.println("entered text: " + textEdit.runForEnteredText());

        screen.refresh();
        screen.stopScreen();
        terminal.close();
        System.exit(0);

    }
    */


}
