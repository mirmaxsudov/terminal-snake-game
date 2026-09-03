package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.List;

public final class ConfirmationScreen {
    private static final int WIDTH = 46;

    public List<String> render(Theme theme, String title, String message) {
        return List.of(
                UiSupport.top(theme, WIDTH),
                UiSupport.line(theme, UiSupport.centered(
                        theme.titleColor() + Ansi.BOLD + title + Ansi.RESET, WIDTH), WIDTH),
                UiSupport.separator(theme, WIDTH),
                UiSupport.line(theme, "", WIDTH),
                UiSupport.line(theme, UiSupport.centered(theme.textColor() + message + Ansi.RESET,
                        WIDTH), WIDTH),
                UiSupport.line(theme, UiSupport.centered(
                        theme.accentColor() + Ansi.BOLD + "Y Confirm    N / Esc Cancel" + Ansi.RESET,
                        WIDTH), WIDTH),
                UiSupport.line(theme, "", WIDTH),
                UiSupport.bottom(theme, WIDTH)
        );
    }
}
