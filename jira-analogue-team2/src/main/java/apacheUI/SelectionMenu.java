package apacheUI;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SelectionMenu extends KeyBoardTUIElement {

    interface SelectionMenuListener {
        /**
         * Called if user pressed enter to select an item.
         * @param itemIndex index of the selected item in the selection list.
         * @return false if SelectionMenu should be closed, true if user can select another item.
         */
        boolean onItemSelected(int itemIndex);
    }

    private List<String> selection = new ArrayList<>();
    private int index = 0;
    private int selectedIndex = -1;
    private SelectionMenuListener listener = null;

    SelectionMenu(Terminal terminal, Screen screen, TextGraphics tg) {
        super(terminal, screen, tg);
    }

    SelectionMenu(Terminal terminal, Screen screen, TextGraphics tg, List<String> selection) {
        super(terminal, screen, tg);
        if (selection != null)
            this.selection = selection;
    }

    SelectionMenu(Terminal terminal, Screen screen, TextGraphics tg, List<String> selection, SelectionMenuListener listener) {
        super(terminal, screen, tg);
        if (selection != null)
            this.selection = selection;
        this.listener = listener;
    }

    void setSelection(List<String> selection) {
        if (selection != null)
            this.selection = selection;
        else
            this.selection.clear();
    }

    void setSelectionMenuListener(SelectionMenuListener listener) {
        this.listener = listener;
    }

    @Override
    boolean onKeyHit(KeyStroke keyStroke) {
        switch ((keyStroke.getKeyType())) {
            case Escape:
                selectedIndex = -1;
                if (listener != null) listener.onItemSelected(-1);
                return false;
            case ArrowUp:
                if (index < selection.size()-1) {
                    index += 1;
                    tg.putString(6, 7, "                            ");
                    tg.putString(6, 7, selection.get(index), SGR.BOLD);
                    break;
                } else if (!selection.isEmpty()) {
                    tg.putString(6, 7, "                            ");
                    tg.putString(6, 7, selection.get(index), SGR.BOLD);
                    break;
                }

            case ArrowDown:
                if (index > 0) {
                    index -= 1;
                    tg.putString(6, 7, "                            ");
                    tg.putString(6, 7, selection.get(index), SGR.BOLD);
                    break;
                } else if (!selection.isEmpty()) {
                    tg.putString(6, 7, "                            ");
                    tg.putString(6, 7, selection.get(index), SGR.BOLD);
                    break;
                }
            case Enter:
                if (!selection.isEmpty()) {
                    tg.putString(6, 8, "                                                                                      ");
                    tg.putString(6, 8, "you have selected: " + selection.get(index), SGR.ITALIC);
                    selectedIndex = index;
                    if (listener != null)
                        return listener.onItemSelected(index);
                }
                break;
            /*case ArrowRight: //should open by just pressing enter
                if (hit) {
                    tg.putString(0, 7, "opening project...", SGR.BOLD);
                    screen.refresh();

                    TimeUnit.SECONDS.sleep(2);
                    tg.putString(0, 7, "                               ");
                    //insert actual opening of project
                    //method here
                    //
                    //method here
                    break;
                }*/
            case EOF:
                selectedIndex = -1;
                if (listener != null) listener.onItemSelected(-1);
                return false;
            default:
                break;
        }
        return true;
    }

    /**
     * Blocks and lets user select an item from the list until user presses ESC or enter to select an item.
     * @return the index of the item in the list selection or -1 if no item was selected.
     */
    public int runForSelectedItemIndex() {
        super.run();
        return selectedIndex;
    }

}
