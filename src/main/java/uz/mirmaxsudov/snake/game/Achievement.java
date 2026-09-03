package uz.mirmaxsudov.snake.game;

public enum Achievement {
    FIRST_BITE("First Bite", "Eat your first food"),
    CENTURY("Century", "Score at least 100 points"),
    LONG_SNAKE("Long Snake", "Reach a length of 20"),
    SURVIVOR("Survivor", "Play for two active minutes"),
    LEVEL_THREE("Level Three", "Reach level 3"),
    COMBO_MASTER("Combo Master", "Build a combo of 5");

    private final String title;
    private final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() { return title; }
    public String description() { return description; }

    public boolean earned(Game game) {
        GameStatistics stats = game.statistics();
        return switch (this) {
            case FIRST_BITE -> stats.foodEaten() >= 1;
            case CENTURY -> game.score() >= 100;
            case LONG_SNAKE -> stats.maximumLength() >= 20;
            case SURVIVOR -> stats.playTimeMillis() >= 120_000;
            case LEVEL_THREE -> stats.level() >= 3;
            case COMBO_MASTER -> stats.maximumCombo() >= 5;
        };
    }
}
