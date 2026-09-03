package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.List;

public final class TextInputScreen {
    private static final int WIDTH = 52;

    public List<String> render(Theme theme, String title, String value, String hint) {
        String field = "[ " + value + "_" + " ".repeat(Math.max(0, 28 - value.length())) + "]";
        return List.of(
                UiSupport.top(theme, WIDTH),
                UiSupport.line(theme, UiSupport.centered(
                        theme.titleColor() + Ansi.BOLD + title + Ansi.RESET, WIDTH), WIDTH),
                UiSupport.separator(theme, WIDTH),
                UiSupport.line(theme, "", WIDTH),
                UiSupport.line(theme, UiSupport.centered(theme.accentColor() + field + Ansi.RESET,
                        WIDTH), WIDTH),
                UiSupport.line(theme, UiSupport.centered(theme.mutedColor() + hint + Ansi.RESET,
                        WIDTH), WIDTH),
                UiSupport.line(theme, UiSupport.centered(
                        theme.mutedColor() + "Enter Save    Esc Cancel" + Ansi.RESET, WIDTH), WIDTH),
                UiSupport.line(theme, "", WIDTH),
                UiSupport.bottom(theme, WIDTH)
        );
    }
}
