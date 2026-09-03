package uz.mirmaxsudov.snake.ui;

import uz.mirmaxsudov.snake.terminal.Ansi;

public final class UiSupport {
    private UiSupport() {
    }

    public static String top(Theme theme, int innerWidth) {
        return theme.borderColor() + "╭" + "─".repeat(innerWidth) + "╮" + Ansi.RESET;
    }

    public static String separator(Theme theme, int innerWidth) {
        return theme.borderColor() + "├" + "─".repeat(innerWidth) + "┤" + Ansi.RESET;
    }

    public static String bottom(Theme theme, int innerWidth) {
        return theme.borderColor() + "╰" + "─".repeat(innerWidth) + "╯" + Ansi.RESET;
    }

    public static String line(Theme theme, String styledContent, int innerWidth) {
        int padding = Math.max(0, innerWidth - Ansi.visibleLength(styledContent));
        return theme.borderColor() + "│" + Ansi.RESET + styledContent + " ".repeat(padding)
                + theme.borderColor() + "│" + Ansi.RESET;
    }

    public static String centered(String text, int width) {
        int visible = Ansi.visibleLength(text);
        int left = Math.max(0, (width - visible) / 2);
        int right = Math.max(0, width - visible - left);
        return " ".repeat(left) + text + " ".repeat(right);
    }
}
