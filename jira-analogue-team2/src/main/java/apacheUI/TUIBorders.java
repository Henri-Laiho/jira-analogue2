package apacheUI;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TUIBorders extends TUIElement {
    TUIBorders(Screen screen, TextGraphics bg) {
        super(screen, bg);
    }

    private TextColor.ANSI setRandomColor() {
        List<TextColor.ANSI> textColors = Arrays.asList(TextColor.ANSI.values());
        return textColors.get((int) (Math.random() * textColors.size()));
    }

    private void tuiBorderDisplay() throws InterruptedException {
        bg.setForegroundColor(setRandomColor());
        //idea for a flashy UI if I get time later: bunch of loops that create a border that changes color, fades away into black, fades into new color, fades away.. etc, might be easier with another TUI
        //Lanterna operates on three layers: terminal, background, foreground, this could be useful in designing an UI
        for (int i = 0; i < 10; i++) {
            bg.putString(4, i+2, "|");
            bg.putString(69, i+2, "|");
        }
        bg.putString(4, 2, "==================================================================");
        bg.putString(4, 12, "==================================================================");



    }

    void runBorder() throws InterruptedException {
        tuiBorderDisplay();
        super.run();
    }
}
