package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.game.Achievement;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AchievementScreen {
    private static final int WIDTH = 58;

    public List<String> render(Theme theme, Set<Achievement> unlocked) {
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "ACHIEVEMENTS " + unlocked.size() + "/"
                        + Achievement.values().length + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        for (Achievement achievement : Achievement.values()) {
            String marker = unlocked.contains(achievement) ? "[✓] " : "[ ] ";
            String color = unlocked.contains(achievement) ? theme.accentColor() : theme.mutedColor();
            lines.add(UiSupport.line(theme, "  " + color + marker + String.format("%-16s", achievement.title())
                    + achievement.description() + Ansi.RESET, WIDTH));
        }
        lines.add(UiSupport.line(theme, "", WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered("Esc / Enter  Back", WIDTH), WIDTH));
        lines.add(UiSupport.bottom(theme, WIDTH));
        return lines;
    }
}
