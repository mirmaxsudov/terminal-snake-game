package uz.mirmaxsudov.snake.game;

public enum PowerUpType {
    SHIELD("Shield", 8_000),
    SCORE_BOOST("2x Score", 8_000),
    PHASE("Phase", 7_000),
    MAGNET("Magnet", 8_000);

    private final String label;
    private final long durationMillis;

    PowerUpType(String label, long durationMillis) {
        this.label = label;
        this.durationMillis = durationMillis;
    }

    public String label() {
        return label;
    }

    public long durationMillis() {
        return durationMillis;
    }
}
