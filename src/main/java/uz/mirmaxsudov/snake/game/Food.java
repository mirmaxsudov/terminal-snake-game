package uz.mirmaxsudov.snake.game;

import java.util.Objects;

public record Food(Position position, FoodType type) {
    public Food {
        Objects.requireNonNull(position);
        Objects.requireNonNull(type);
    }

    public Food(Position position) {
        this(position, FoodType.NORMAL);
    }
}
