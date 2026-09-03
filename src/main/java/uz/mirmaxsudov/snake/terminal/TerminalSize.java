package uz.mirmaxsudov.snake.terminal;

public record TerminalSize(int columns, int rows) {
    public boolean atLeast(int minimumColumns, int minimumRows) {
        return columns >= minimumColumns && rows >= minimumRows;
    }
}
