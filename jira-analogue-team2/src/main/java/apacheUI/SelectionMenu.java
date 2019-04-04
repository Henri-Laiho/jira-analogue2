package apacheUI;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

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

    void setOnItemSelectedListener(SelectionMenuListener listener) {
        this.listener = listener;
    }

    @Override
    boolean onKeyHit(KeyStroke keyStroke) {
        switch ((keyStroke.getKeyType())) {
            case Escape:
                if (listener != null) listener.onItemSelected(-1);
                return false;
            case ArrowUp:
                if (index < selection.size()-1) {
                    index += 1;
                    tg.putString(0, 3, "                            ");
                    tg.putString(0, 3, selection.get(index), SGR.BOLD);
                    break;
                } else if (!selection.isEmpty()) {
                    tg.putString(0, 3, "                            ");
                    tg.putString(0, 3, selection.get(index), SGR.BOLD);
                    break;
                }

            case ArrowDown:
                if (index > 0) {
                    index -= 1;
                    tg.putString(0, 3, "                            ");
                    tg.putString(0, 3, selection.get(index), SGR.BOLD);
                    break;
                } else if (!selection.isEmpty()) {
                    tg.putString(0, 3, "                            ");
                    tg.putString(0, 3, selection.get(index), SGR.BOLD);
                    break;
                }
            case Enter:
                if (!selection.isEmpty()) {
                    tg.putString(0, 4, "                                                                                      ");
                    tg.putString(0, 4, "you have selected: " + selection.get(index), SGR.ITALIC);
                    if (listener != null) listener.onItemSelected(index);
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
                if (listener != null) listener.onItemSelected(-1);
                return false;
            default:
                break;
        }
        return true;
    }

}
