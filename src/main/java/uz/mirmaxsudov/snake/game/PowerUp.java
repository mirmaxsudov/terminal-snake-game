package uz.mirmaxsudov.snake.game;

import java.util.Objects;

public record PowerUp(Position position, PowerUpType type) {
    public PowerUp {
        Objects.requireNonNull(position);
        Objects.requireNonNull(type);
    }
}
