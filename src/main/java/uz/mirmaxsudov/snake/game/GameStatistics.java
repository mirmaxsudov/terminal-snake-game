package uz.mirmaxsudov.snake.game;

public record GameStatistics(
        int foodEaten,
        int maximumLength,
        long playTimeMillis,
        double averagePointsPerFood
) {
}
