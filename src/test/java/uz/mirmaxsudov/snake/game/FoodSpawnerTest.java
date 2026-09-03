package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodSpawnerTest {
    @Test
    void alwaysSpawnsInsideBoardAndOutsideSnake() {
        Snake snake = Snake.of(List.of(
                new Position(3, 2), new Position(2, 2), new Position(1, 2)), Direction.RIGHT);
        Random random = new Random(42);

        for (int i = 0; i < 200; i++) {
            Food food = FoodSpawner.spawn(8, 6, snake, random).orElseThrow();
            Position position = food.position();
            assertTrue(position.x() >= 0 && position.x() < 8);
            assertTrue(position.y() >= 0 && position.y() < 6);
            assertFalse(snake.occupies(position));
        }
    }
}
