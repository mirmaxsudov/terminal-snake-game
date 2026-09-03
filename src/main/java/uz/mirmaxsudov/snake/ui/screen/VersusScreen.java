package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.game.AiDifficulty;
import uz.mirmaxsudov.snake.game.FoodType;
import uz.mirmaxsudov.snake.game.GameMode;
import uz.mirmaxsudov.snake.game.MatchStatus;
import uz.mirmaxsudov.snake.game.Position;
import uz.mirmaxsudov.snake.game.VersusGame;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.SnakeStyle;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VersusScreen {
    public List<String> render(VersusGame game, Theme theme, AiDifficulty aiDifficulty,
                               SnakeStyle snakeStyle, boolean pulse, int countdown) {
        int width = game.width() * 2;
        String opponent = game.mode() == GameMode.VS_AI ? "AI " + aiDifficulty.label() : "Player 2";
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, width));
        lines.add(UiSupport.line(theme, UiSupport.centered(theme.titleColor() + Ansi.BOLD
                + "SNAKE ARENA · " + opponent.toUpperCase() + Ansi.RESET, width), width));
        lines.add(UiSupport.separator(theme, width));

        Set<Position> playerOneBody = new HashSet<>(game.playerOne().body());
        Set<Position> playerTwoBody = new HashSet<>(game.playerTwo().body());
        Set<Position> obstacles = game.obstacles();
        List<String> overlay = overlay(game, theme, opponent, countdown);
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
                if (position.equals(game.playerOne().head())) {
                    row.append(theme.snakeHeadColor()).append(snakeStyle.head()).append(Ansi.RESET);
                } else if (playerOneBody.contains(position)) {
                    row.append(theme.snakeColor()).append(snakeStyle.body()).append(Ansi.RESET);
                } else if (position.equals(game.playerTwo().head())) {
                    row.append(theme.titleColor()).append(Ansi.BOLD).append(snakeStyle.head()).append(Ansi.RESET);
                } else if (playerTwoBody.contains(position)) {
                    row.append(theme.accentColor()).append(snakeStyle.body()).append(Ansi.RESET);
                } else if (obstacles.contains(position)) {
                    row.append(theme.borderColor()).append("▓▓").append(Ansi.RESET);
                } else if (game.food() != null && position.equals(game.food().position())) {
                    String glyph = game.food().type() == FoodType.BONUS ? "★ " : "● ";
                    row.append(game.food().type() == FoodType.BONUS
                                    ? theme.accentColor() : theme.foodColor())
                            .append(pulse ? Ansi.BOLD : "").append(glyph).append(Ansi.RESET);
                } else row.append("  ");
            }
            lines.add(UiSupport.line(theme, row.toString(), width));
        }

        lines.add(UiSupport.separator(theme, width));
        String scores = width < 50
                ? String.format("P1 %04d  P2 %04d  x%d", game.playerOneScore(),
                        game.playerTwoScore(), game.freshnessMultiplier())
                : String.format(" PLAYER 1 %05d       %-10s %05d       x%d ",
                        game.playerOneScore(), opponent.toUpperCase(), game.playerTwoScore(),
                        game.freshnessMultiplier());
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.textColor() + Ansi.BOLD + scores + Ansi.RESET, width), width));
        String controls = game.mode() == GameMode.TWO_PLAYER
                ? width < 50 ? "P1 WASD  P2 ARROWS  P Pause  M Menu"
                        : "P1 WASD  ·  P2 ARROWS  ·  P Pause  ·  M Menu"
                : width < 50 ? "WASD/Arrows  P Pause  M Menu"
                        : "WASD / ARROWS Move  ·  P Pause  ·  M Menu";
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.mutedColor() + controls + Ansi.RESET, width), width));
        lines.add(UiSupport.bottom(theme, width));
        return lines;
    }

    private List<String> overlay(VersusGame game, Theme theme, String opponent, int countdown) {
        if (countdown != 0) {
            String value = countdown > 0 ? Integer.toString(countdown) : "GO!";
            return box(theme, "GET READY", value);
        }
        if (game.status() == MatchStatus.PAUSED) return box(theme, "PAUSED", "Press P to resume");
        if (!game.status().finished()) return List.of();
        String winner = switch (game.status()) {
            case PLAYER_ONE_WON -> "PLAYER 1 WINS";
            case PLAYER_TWO_WON -> opponent.toUpperCase() + " WINS";
            case DRAW -> "DRAW";
            default -> throw new IllegalStateException("Match has not ended");
        };
        return List.of(
                theme.borderColor() + "╭────────────────────────────╮" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.titleColor() + Ansi.BOLD
                        + center(winner, 28) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.textColor()
                        + String.format("  Final score: %5d - %-5d ", game.playerOneScore(),
                        game.playerTwoScore()) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.mutedColor()
                        + "  [R] Rematch  [M] Menu    " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.mutedColor()
                        + "  [Q] Quit                  " + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "╰────────────────────────────╯" + Ansi.RESET
        );
    }

    private List<String> box(Theme theme, String title, String detail) {
        return List.of(
                theme.borderColor() + "╭──────────────────────────╮" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.titleColor() + Ansi.BOLD
                        + center(title, 26) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "│" + Ansi.RESET + theme.accentColor() + Ansi.BOLD
                        + center(detail, 26) + Ansi.RESET + theme.borderColor() + "│" + Ansi.RESET,
                theme.borderColor() + "╰──────────────────────────╯" + Ansi.RESET
        );
    }

    private String center(String value, int width) {
        if (value.length() > width) value = value.substring(0, width);
        int left = Math.max(0, (width - value.length()) / 2);
        return " ".repeat(left) + value + " ".repeat(width - value.length() - left);
    }
}
