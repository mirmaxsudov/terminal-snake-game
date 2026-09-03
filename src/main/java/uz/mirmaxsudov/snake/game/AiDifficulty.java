package uz.mirmaxsudov.snake.game;

public enum AiDifficulty {
    SIMPLE("Simple", "Chooses randomly from safe moves"),
    STRATEGIC("Strategic", "Chases food while avoiding immediate danger"),
    PATHFINDER("Pathfinder", "Uses shortest paths and open-space analysis");

    private final String label;
    private final String description;

    AiDifficulty(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public AiDifficulty next() {
        AiDifficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
