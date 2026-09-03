package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.game.Game;
import uz.mirmaxsudov.snake.game.GameStatus;
import uz.mirmaxsudov.snake.game.GameStatistics;
import uz.mirmaxsudov.snake.game.FoodType;
import uz.mirmaxsudov.snake.game.Position;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;
import uz.mirmaxsudov.snake.ui.SnakeStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GameScreen {
    public List<String> render(Game game, Theme theme, String difficulty, int highScore,
                               boolean newHighScore, boolean pulse) {
        return render(game, theme, difficulty, highScore, newHighScore, pulse, 0,
                SnakeStyle.BLOCKS);
    }

    public List<String> render(Game game, Theme theme, String difficulty, int highScore,
                               boolean newHighScore, boolean pulse, int countdown) {
        return render(game, theme, difficulty, highScore, newHighScore, pulse, countdown,
                SnakeStyle.BLOCKS);
    }

    public List<String> render(Game game, Theme theme, String difficulty, int highScore,
                               boolean newHighScore, boolean pulse, int countdown,
                               SnakeStyle snakeStyle) {
        int width = game.width() * 2;
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, width));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "JAVA SNAKE" + Ansi.RESET, width), width));
        lines.add(UiSupport.separator(theme, width));

        Set<Position> body = new HashSet<>(game.snake().body());
        List<String> overlay = overlay(game, theme, highScore, newHighScore, countdown);
        int overlayStart = overlay.isEmpty() ? -1 : (game.height() - overlay.size()) / 2;
        for (int y = 0; y < game.height(); y++) {
            if (y >= overlayStart && y < overlayStart + overlay.size()) {
                lines.add(UiSupport.line(theme, UiSupport.centered(
                        overlay.get(y - overlayStart), width), width));
                continue;
            }
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < game.width(); x++) {
                Position position = new Position(x, y);
                if (position.equals(game.snake().head())) {
                    row.append(theme.snakeHeadColor()).append(snakeStyle.head()).append(Ansi.RESET);
                } else if (body.contains(position)) {
                    row.append(theme.snakeColor()).append(snakeStyle.body()).append(Ansi.RESET);
                } else if (game.food() != null && position.equals(game.food().position())) {
                    String effect = pulse ? Ansi.BOLD : "";
                    boolean bonus = game.food().type() == FoodType.BONUS;
                    row.append(bonus ? theme.accentColor() : theme.foodColor())
                            .append(effect)
                            .append(bonus ? "★ " : "● ")
                            .append(Ansi.RESET);
                } else {
                    row.append("  ");
                }
            }
            lines.add(UiSupport.line(theme, row.toString(), width));
        }

        lines.add(UiSupport.separator(theme, width));
        String score = String.format(" SCORE %05d   HIGH %05d", game.score(), highScore);
        long foodSeconds = Math.max(1, (game.foodRemainingMillis() + 999) / 1_000);
        String mode = difficulty.toUpperCase() + (game.wrapWalls() ? " WRAP" : "");
        String status = width < 50
                ? String.format("x%d %ds %c%s", game.scoreMultiplier(), foodSeconds,
                        Character.toUpperCase(difficulty.charAt(0)), game.wrapWalls() ? " W" : "")
                : String.format(" x%d  %ds  %s ", game.scoreMultiplier(), foodSeconds, mode);
        int gap = Math.max(1, width - score.length() - status.length());
        lines.add(UiSupport.line(theme, theme.accentColor() + Ansi.BOLD + score + Ansi.RESET
                + " ".repeat(gap) + theme.textColor() + status + Ansi.RESET, width));
        String controls = width < 50
                ? "WASD/Arrows  P Pause  M Menu  Q Quit"
                : "WASD / ARROWS Move  ·  P Pause  ·  M Menu  ·  Q Quit";
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + controls + Ansi.RESET, width), width));
        lines.add(UiSupport.bottom(theme, width));
        return lines;
    }

    private List<String> overlay(Game game, Theme theme, int highScore, boolean newHighScore,
                                 int countdown) {
        if (countdown != 0) {
            String countdownText = countdown > 0 ? Integer.toString(countdown) : "GO!";
            return List.of(
                    theme.borderColor() + "╭──────────────────────────╮" + Ansi.RESET,
                    theme.borderColor() + "│" + Ansi.RESET + theme.titleColor() + Ansi.BOLD
                            + "        GET READY         " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                    theme.borderColor() + "│" + Ansi.RESET + theme.accentColor() + Ansi.BOLD
                            + String.format("%-26s", center(countdownText, 26))
                            + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                    theme.borderColor() + "╰──────────────────────────╯" + Ansi.RESET
            );
        }
        if (game.status() == GameStatus.PAUSED) {
            return List.of(
                    theme.borderColor() + "╭──────────────────────────╮" + Ansi.RESET,
                    theme.borderColor() + "│" + Ansi.RESET + theme.titleColor() + Ansi.BOLD
                            + "          PAUSED          " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                    theme.borderColor() + "│" + Ansi.RESET + theme.mutedColor()
                            + "     Press P to resume    " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                    theme.borderColor() + "╰──────────────────────────╯" + Ansi.RESET
            );
        }
        if (game.status() != GameStatus.GAME_OVER && game.status() != GameStatus.WON) return List.of();
        String title = game.status() == GameStatus.WON ? "YOU WIN!" : "GAME OVER";
        String record = newHighScore ? "      NEW HIGH SCORE!       " : "                            ";
        GameStatistics statistics = game.statistics();
        return List.of(
                theme.borderColor() + "╭────────────────────────────╮" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.titleColor() + Ansi.BOLD
                        + String.format("%-28s", center(title, 28)) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Score:       %-13d", game.score()) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  High Score:  %-13d", highScore) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Food eaten:  %-13d", statistics.foodEaten()) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Max length:  %-13d", statistics.maximumLength()) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Play time:   %-13s", formatDuration(statistics.playTimeMillis())) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Avg points:  %-13.1f", statistics.averagePointsPerFood()) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.accentColor()
                        + record + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.mutedColor()
                        + "  [R] Restart  [M] Menu     " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.mutedColor()
                        + "  [Q] Quit                  " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "╰────────────────────────────╯" + Ansi.RESET
        );
    }

    private String center(String value, int width) {
        int left = Math.max(0, (width - value.length()) / 2);
        return " ".repeat(left) + value;
    }

    private String formatDuration(long millis) {
        long totalSeconds = millis / 1_000;
        return String.format("%dm %02ds", totalSeconds / 60, totalSeconds % 60);
    }
}
