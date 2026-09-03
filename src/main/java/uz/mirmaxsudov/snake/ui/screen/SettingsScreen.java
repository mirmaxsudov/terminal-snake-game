package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.game.BoardSize;
import uz.mirmaxsudov.snake.game.Difficulty;
import uz.mirmaxsudov.snake.game.GameMode;
import uz.mirmaxsudov.snake.game.AiDifficulty;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.SnakeStyle;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.ArrayList;
import java.util.List;

public final class SettingsScreen {
    public static final int ITEM_COUNT = 11;
    private static final int WIDTH = 52;

    public List<String> render(int selected, Difficulty difficulty, BoardSize boardSize,
                               SnakeStyle snakeStyle, Theme theme, boolean wrapWalls,
                               boolean obstacles, String playerName, Long seed,
                               GameMode gameMode, AiDifficulty aiDifficulty) {
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "GAME SETTINGS" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        String[] items = {
                "Player Name    " + playerName,
                "Game Mode      " + gameMode.label(),
                "AI Difficulty  " + (gameMode == GameMode.VS_AI ? aiDifficulty.label() : "—"),
                "Difficulty     " + difficulty.label(),
                "Board Size     " + boardSize.label() + " " + boardSize.width() + "x" + boardSize.height(),
                "Snake Style    " + snakeStyle.label(),
                "Theme          " + theme.name(),
                "Wall Wrap      " + onOff(wrapWalls),
                "Obstacles      " + onOff(obstacles),
                "Game Seed      " + (seed == null ? "Random" : seed),
                "Back"
        };
        for (int i = 0; i < items.length; i++) {
            String marker = i == selected ? " › " : "   ";
            String color = i == selected ? theme.accentColor() + Ansi.BOLD : theme.textColor();
            lines.add(UiSupport.line(theme, "    " + color + marker + items[i] + Ansi.RESET, WIDTH));
        }
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + "Enter Change/Edit    Esc Back" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.bottom(theme, WIDTH));
        return lines;
    }

    private String onOff(boolean value) { return value ? "On" : "Off"; }
}
