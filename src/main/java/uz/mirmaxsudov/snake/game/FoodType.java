package uz.mirmaxsudov.snake.game;

public enum FoodType {
    NORMAL(10),
    BONUS(30);

    private final int points;

    FoodType(int points) {
        this.points = points;
    }

    public int points() {
        return points;
    }
}
