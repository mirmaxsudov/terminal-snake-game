package uz.mirmaxsudov.snake.game;

public enum FoodType {
    NORMAL("Normal", 10, true),
    BONUS("Bonus", 30, true),
    SPEED("Speed", 15, true),
    SLOW("Slow", 10, true),
    SHRINK("Shrink", 5, false),
    POISON("Poison", -20, false);

    private final String label;
    private final int points;
    private final boolean growsSnake;

    FoodType(String label, int points, boolean growsSnake) {
        this.label = label;
        this.points = points;
        this.growsSnake = growsSnake;
    }

    public String label() { return label; }
    public int points() { return points; }
    public boolean growsSnake() { return growsSnake; }
}
