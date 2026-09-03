package uz.mirmaxsudov.snake.game;

public enum GameMode {
    SOLO("Solo"),
    VS_AI("Vs AI"),
    TWO_PLAYER("Two Players");

    private final String label;

    GameMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public GameMode next() {
        GameMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
