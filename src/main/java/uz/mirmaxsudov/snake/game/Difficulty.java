package uz.mirmaxsudov.snake.game;

public enum Difficulty {
    EASY("Easy", 150), NORMAL("Normal", 100), HARD("Hard", 65);

    private final String label;
    private final long tickMillis;

    Difficulty(String label, long tickMillis) {
        this.label = label;
        this.tickMillis = tickMillis;
    }

    public String label() {
        return label;
    }

    public long tickMillis() {
        return tickMillis;
    }

    public Difficulty next() {
        Difficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
