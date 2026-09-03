package uz.mirmaxsudov.snake.ui;

import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.game.GameMode;

import java.util.ArrayList;
import java.util.List;

public final class MainMenu {
    public static final int ITEM_COUNT = 7;
    private static final int WIDTH = 44;

    public List<String> render(int selected, Theme theme, int highScore, boolean replayAvailable) {
        return render(selected, theme, highScore, replayAvailable, GameMode.SOLO);
    }

    public List<String> render(int selected, Theme theme, int highScore, boolean replayAvailable,
                               GameMode gameMode) {
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "JAVA SNAKE" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + "A modern terminal classic" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, "", WIDTH));
        String[] items = {
                "Start Game  [" + gameMode.label() + "]", "Settings", "Leaderboard", "Achievements",
                replayAvailable ? "Replay Last Game" : "Replay Last Game  (empty)", "Help", "Exit"
        };
        for (int i = 0; i < items.length; i++) {
            String marker = i == selected ? "  › " : "    ";
            String color = i == selected ? theme.accentColor() + Ansi.BOLD : theme.textColor();
            lines.add(UiSupport.line(theme, "      " + color + marker + items[i] + Ansi.RESET, WIDTH));
        }
        lines.add(UiSupport.line(theme, "", WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + "↑ ↓ Navigate    Enter Select" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + "High score  " + String.format("%05d", highScore) + Ansi.RESET,
                WIDTH), WIDTH));
        lines.add(UiSupport.bottom(theme, WIDTH));
        return lines;
    }
}
