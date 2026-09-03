package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.storage.LeaderboardEntry;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.ArrayList;
import java.util.List;

public final class LeaderboardScreen {
    private static final int WIDTH = 58;

    public List<String> render(Theme theme, List<LeaderboardEntry> entries) {
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "LOCAL LEADERBOARD" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, "  #  PLAYER        SCORE  DIFFICULTY  LEVEL  DATE", WIDTH));
        if (entries.isEmpty()) lines.add(UiSupport.line(theme, "  No completed games yet.", WIDTH));
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            lines.add(UiSupport.line(theme, String.format("  %2d %-12.12s %6d  %-10.10s %5d  %s",
                    i + 1, entry.player(), entry.score(), entry.difficulty(), entry.level(), entry.date()), WIDTH));
        }
        while (lines.size() < 15) lines.add(UiSupport.line(theme, "", WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered("Esc / Enter  Back", WIDTH), WIDTH));
        lines.add(UiSupport.bottom(theme, WIDTH));
        return lines;
    }
}
