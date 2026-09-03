package uz.mirmaxsudov.snake.game;

public enum BoardSize {
    SMALL("Small", 20, 12),
    MEDIUM("Medium", 28, 16),
    LARGE("Large", 40, 20);

    private final String label;
    private final int width;
    private final int height;

    BoardSize(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    public String label() { return label; }
    public int width() { return width; }
    public int height() { return height; }

    public BoardSize next() {
        BoardSize[] sizes = values();
        return sizes[(ordinal() + 1) % sizes.length];
    }
}
