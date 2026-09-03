package uz.mirmaxsudov.snake.ui;

public enum SnakeStyle {
    BLOCKS("Blocks", "██", "██"),
    CIRCLES("Circles", "● ", "○ "),
    ASCII("ASCII", "@@", "##"),
    DIAMONDS("Diamonds", "◆ ", "◇ ");

    private final String label;
    private final String head;
    private final String body;

    SnakeStyle(String label, String head, String body) {
        this.label = label;
        this.head = head;
        this.body = body;
    }

    public String label() { return label; }
    public String head() { return head; }
    public String body() { return body; }

    public SnakeStyle next() {
        SnakeStyle[] styles = values();
        return styles[(ordinal() + 1) % styles.length];
    }
}
