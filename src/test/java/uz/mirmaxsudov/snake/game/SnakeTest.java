package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeTest {
    @Test
    void movesHeadAndRemovesTail() {
        Snake snake = Snake.of(List.of(
                new Position(3, 2), new Position(2, 2), new Position(1, 2)), Direction.RIGHT);

        snake.move(Direction.RIGHT, false);

        assertEquals(List.of(new Position(4, 2), new Position(3, 2), new Position(2, 2)), snake.body());
    }

    @Test
    void growthKeepsTail() {
        Snake snake = Snake.of(List.of(
                new Position(3, 2), new Position(2, 2), new Position(1, 2)), Direction.RIGHT);

        snake.move(Direction.RIGHT, true);

        assertEquals(4, snake.length());
        assertTrue(snake.occupies(new Position(1, 2)));
    }

    @Test
    void movingIntoVacatingTailIsSafeButOtherBodySegmentsCollide() {
        Snake snake = Snake.of(List.of(
                new Position(2, 2), new Position(1, 2), new Position(1, 1), new Position(2, 1)),
                Direction.DOWN);

        assertFalse(snake.wouldHitSelf(new Position(2, 1), false));
        assertTrue(snake.wouldHitSelf(new Position(1, 2), false));
        assertTrue(snake.wouldHitSelf(new Position(2, 1), true));
    }
}
