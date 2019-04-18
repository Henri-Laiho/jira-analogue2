package LanternaUI;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextEditor extends KeyBoardTUIElement {

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

    private TextBox textBox;

    TextEditor(Terminal terminal, Screen screen, TextGraphics tg, Panel panel, int rows, int columns, boolean single_line) {
        super(terminal, screen, tg);
        text.add(new StringBuilder());
        if (rows > 1) max_visible_rows = rows;
        if (columns > 1) max_visible_columns = columns;
        this.single_line = single_line;
        textBox = new TextBox(new TerminalSize(columns, rows), "", single_line ? TextBox.Style.SINGLE_LINE : TextBox.Style.MULTI_LINE);
    }

    void setTextEditListener(TextEditListener listener) {
        this.listener = listener;
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
    }

    @Override
    boolean onKeyHit(KeyStroke keyStroke) {
        Interactable.Result result = textBox.handleKeyStroke(keyStroke);
        return result == Interactable.Result.HANDLED;
    }

    public List<String> runForEnteredText() {
        super.run();
        return enteredText;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Opening terminal.");

        DefaultTerminalFactory dtf = new DefaultTerminalFactory();


        Terminal terminal = dtf.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        TextGraphics tg = screen.newTextGraphics();

        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
        final Window window = new BasicWindow("My Root Window");
        Panel contentPanel = new Panel(new GridLayout(2));
        GridLayout gridLayout = (GridLayout)contentPanel.getLayoutManager();
        gridLayout.setHorizontalSpacing(3);
        Label title = new Label("This is a label that spans two columns");
        title.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                true,       // Give the component extra horizontal space if available
                false,        // Give the component extra vertical space if available
                2,                  // Horizontal span
                1));                  // Vertical span
        contentPanel.addComponent(title);

        screen.startScreen();
        tg.putString(0, 1, "welcome to the minjira text user interface");
        tg.putString(0, 22, "hit ESC to exit");
        screen.refresh();

        TextEditor textEditor = new TextEditor(terminal, screen, tg, contentPanel, 7, 32, false);
        textEditor.setPosition(2, 1);

        window.setComponent(contentPanel);

        contentPanel.addComponent(textEditor.textBox);
        System.out.println("entered text: " + textEditor.runForEnteredText());

        screen.refresh();
        screen.stopScreen();
        terminal.close();
        System.exit(0);

    }


}
