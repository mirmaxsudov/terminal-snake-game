package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.terminal.TerminalSize;
import uz.mirmaxsudov.snake.ui.Theme;

import java.util.List;

public final class ResizeScreen {
    public List<String> render(TerminalSize size, Theme theme, int minimumColumns, int minimumRows) {
        return List.of(
                theme.titleColor() + Ansi.BOLD + "Terminal too small." + Ansi.RESET,
                "",
                theme.textColor() + "Minimum: " + minimumColumns + "x" + minimumRows + Ansi.RESET,
                theme.textColor() + "Current: " + size.columns() + "x" + size.rows() + Ansi.RESET,
                "",
                theme.mutedColor() + "Resize the terminal to continue." + Ansi.RESET,
                theme.mutedColor() + "Press Q to quit." + Ansi.RESET
        );
    }
}
