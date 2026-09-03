package uz.mirmaxsudov.snake.game;

import java.util.Optional;
import java.util.random.RandomGenerator;

public final class FoodSpawner {
    private FoodSpawner() {}

    public static Optional<Food> spawn(int width, int height, Snake snake, RandomGenerator random) {
        return spawn(width, height, snake, random, FoodType.NORMAL);
    }

    public static Optional<Food> spawn(int width, int height, Snake snake, RandomGenerator random,
                                       FoodType type) {
        int freeCells = width * height - snake.length();
        if (freeCells <= 0) return Optional.empty();
        int target = random.nextInt(freeCells);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position candidate = new Position(x, y);
                if (!snake.occupies(candidate) && target-- == 0) return Optional.of(new Food(candidate, type));
            }
        }
        throw new IllegalStateException("Failed to locate a free food cell");
    }
}
