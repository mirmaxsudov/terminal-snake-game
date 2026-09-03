package uz.mirmaxsudov.snake.ui.screen;

import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.UiSupport;

import java.util.ArrayList;
import java.util.List;

public final class HelpScreen {
    private static final int WIDTH = 50;

    public List<String> render(Theme theme) {
        List<String> lines = new ArrayList<>();
        lines.add(UiSupport.top(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.titleColor() + Ansi.BOLD + "HOW TO PLAY" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(row(theme, "WASD", "Solo / Player 1 movement"));
        lines.add(row(theme, "ARROWS", "Solo / Player 2 movement"));
        lines.add(row(theme, "P", "Pause or resume"));
        lines.add(row(theme, "M / Esc", "Return to menu"));
        lines.add(row(theme, "Q", "Quit (asks for confirmation)"));
        lines.add(row(theme, "Y / N", "Answer a confirmation prompt"));
        lines.add(UiSupport.line(theme, "", WIDTH));
        lines.add(UiSupport.line(theme, "   Eat the food, grow longer, and avoid walls", WIDTH));
        lines.add(UiSupport.line(theme, "   and your own body. Reversals are ignored.", WIDTH));
        lines.add(UiSupport.line(theme, "   Eat quickly for x3 or x2 score multipliers.", WIDTH));
        lines.add(UiSupport.line(theme, "   Food expires after 9s. Stars give 30 points.", WIDTH));
        lines.add(UiSupport.line(theme, "   Wall Wrap can be enabled in Settings.", WIDTH));
        lines.add(UiSupport.line(theme, "   Board size, snake style, and accessible", WIDTH));
        lines.add(UiSupport.line(theme, "   color themes are also available there.", WIDTH));
        lines.add(UiSupport.line(theme, "   Arena modes add enemy AI or local Player 2.", WIDTH));
        lines.add(UiSupport.line(theme, "   AI tiers: Simple, Strategic, and Pathfinder.", WIDTH));
        lines.add(UiSupport.line(theme, "   Food: ● normal ★ bonus » speed ◌ slow", WIDTH));
        lines.add(UiSupport.line(theme, "         ▼ shrink × poison; power-ups: S 2× P M", WIDTH));
        lines.add(UiSupport.separator(theme, WIDTH));
        lines.add(UiSupport.line(theme, UiSupport.centered(
                theme.accentColor() + "Esc / Enter  Back" + Ansi.RESET, WIDTH), WIDTH));
        lines.add(UiSupport.bottom(theme, WIDTH));
        return lines;
    }

    private String row(Theme theme, String key, String description) {
        String content = "   " + theme.accentColor() + Ansi.BOLD
                + String.format("%-10s", key) + Ansi.RESET + theme.textColor() + description + Ansi.RESET;
        return UiSupport.line(theme, content, WIDTH);
    }
}
