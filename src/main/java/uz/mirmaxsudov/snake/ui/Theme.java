package uz.mirmaxsudov.snake.ui;

import uz.mirmaxsudov.snake.terminal.Ansi;

import java.util.List;

public record Theme(
        String name,
        String snakeColor,
        String snakeHeadColor,
        String foodColor,
        String borderColor,
        String titleColor,
        String textColor,
        String accentColor,
        String mutedColor
) {
    public static final Theme DEFAULT = new Theme("Default",
            Ansi.rgb(45, 212, 191), Ansi.rgb(94, 234, 212), Ansi.rgb(251, 113, 133),
            Ansi.rgb(71, 85, 105), Ansi.rgb(167, 139, 250), Ansi.rgb(226, 232, 240),
            Ansi.rgb(56, 189, 248), Ansi.rgb(148, 163, 184));

    public static final Theme MATRIX = new Theme("Matrix",
            Ansi.rgb(34, 197, 94), Ansi.rgb(134, 239, 172), Ansi.rgb(250, 204, 21),
            Ansi.rgb(22, 101, 52), Ansi.rgb(74, 222, 128), Ansi.rgb(187, 247, 208),
            Ansi.rgb(34, 197, 94), Ansi.rgb(110, 231, 183));

    public static final Theme OCEAN = new Theme("Ocean",
            Ansi.rgb(14, 165, 233), Ansi.rgb(103, 232, 249), Ansi.rgb(251, 146, 60),
            Ansi.rgb(30, 64, 175), Ansi.rgb(56, 189, 248), Ansi.rgb(224, 242, 254),
            Ansi.rgb(45, 212, 191), Ansi.rgb(125, 211, 252));

    public static final Theme MONOCHROME = new Theme("Monochrome",
            Ansi.rgb(203, 213, 225), Ansi.rgb(255, 255, 255), Ansi.rgb(148, 163, 184),
            Ansi.rgb(100, 116, 139), Ansi.rgb(255, 255, 255), Ansi.rgb(226, 232, 240),
            Ansi.rgb(255, 255, 255), Ansi.rgb(148, 163, 184));

    public static final Theme HIGH_CONTRAST = new Theme("High Contrast",
            Ansi.rgb(255, 255, 255), Ansi.rgb(0, 255, 255), Ansi.rgb(255, 255, 0),
            Ansi.rgb(255, 255, 255), Ansi.rgb(0, 255, 255), Ansi.rgb(255, 255, 255),
            Ansi.rgb(255, 255, 0), Ansi.rgb(220, 220, 220));

    // Okabe-Ito inspired palette, chosen to remain distinguishable for common color-vision deficiencies.
    public static final Theme COLOR_SAFE = new Theme("Color Safe",
            Ansi.rgb(0, 114, 178), Ansi.rgb(86, 180, 233), Ansi.rgb(230, 159, 0),
            Ansi.rgb(204, 121, 167), Ansi.rgb(86, 180, 233), Ansi.rgb(240, 240, 240),
            Ansi.rgb(240, 228, 66), Ansi.rgb(180, 180, 180));

    private static final List<Theme> ALL = List.of(
            DEFAULT, MATRIX, OCEAN, MONOCHROME, HIGH_CONTRAST, COLOR_SAFE);

    public Theme next() {
        return ALL.get((ALL.indexOf(this) + 1) % ALL.size());
    }
}
