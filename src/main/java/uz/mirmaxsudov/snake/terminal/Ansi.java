package uz.mirmaxsudov.snake.terminal;

import java.util.regex.Pattern;

public final class Ansi {
    public static final String ESC = "\u001B[";
    public static final String RESET = ESC + "0m";
    public static final String BOLD = ESC + "1m";
    public static final String DIM = ESC + "2m";
    public static final String CLEAR_SCREEN = ESC + "2J";
    public static final String CLEAR_LINE = ESC + "2K";
    public static final String HOME = ESC + "H";
    public static final String HIDE_CURSOR = ESC + "?25l";
    public static final String SHOW_CURSOR = ESC + "?25h";
    public static final String ALT_SCREEN = ESC + "?1049h";
    public static final String MAIN_SCREEN = ESC + "?1049l";
    public static final String DISABLE_WRAP = ESC + "?7l";
    public static final String ENABLE_WRAP = ESC + "?7h";
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\x1B\\[[0-?]*[ -/]*[@-~]");

    private Ansi() {
    }

    public static String rgb(int red, int green, int blue) {
        return ESC + "38;2;" + red + ";" + green + ";" + blue + "m";
    }

    public static String cursor(int row, int column) {
        return ESC + row + ";" + column + "H";
    }

    public static String paint(String color, String text) {
        return color + text + RESET;
    }

    public static int visibleLength(String text) {
        String plain = ANSI_PATTERN.matcher(text).replaceAll("");
        return plain.codePointCount(0, plain.length());
    }
}
