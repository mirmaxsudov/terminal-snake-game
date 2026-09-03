package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {
    @Test
    void detectsWallCollision() {
        Game game = new Game(8, 6, new Random(1));

        for (int i = 0; i < 4; i++) game.tick();

        assertEquals(GameStatus.GAME_OVER, game.status());
    }

    @Test
    void detectsSelfCollision() {
        Snake snake = Snake.of(List.of(
                new Position(2, 2), new Position(1, 2), new Position(1, 1),
                new Position(2, 1), new Position(3, 1)), Direction.DOWN);
        Game game = new Game(8, 6, snake, new Food(new Position(7, 5)), new Random(1));

        game.requestDirection(Direction.LEFT);
        game.tick();

        assertEquals(GameStatus.GAME_OVER, game.status());
    }

    @Test
    void rejectsInstantReverseDirection() {
        Game game = new Game(10, 8, new Random(1));
        Position initialHead = game.snake().head();

        game.requestDirection(Direction.LEFT);
        game.tick();

        assertEquals(Direction.RIGHT, game.snake().direction());
        assertEquals(initialHead.translate(Direction.RIGHT), game.snake().head());
    }

    @Test
    void acceptsOnlyOneTurnPerTickToPreventQueuedReverse() {
        Game game = new Game(10, 8, new Random(1));
        Position initialHead = game.snake().head();

        game.requestDirection(Direction.UP);
        game.requestDirection(Direction.LEFT);
        game.tick();

        assertEquals(Direction.UP, game.snake().direction());
        assertEquals(initialHead.translate(Direction.UP), game.snake().head());
    }

    @Test
    void eatingGrowsSnakeAndIncreasesScore() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)), new Random(1));

        game.advanceTime(1_250);
        game.tick();

        assertEquals(Game.POINTS_PER_FOOD * 3, game.score());
        assertEquals(5, game.snake().length());
        assertEquals(1, game.statistics().foodEaten());
        assertEquals(5, game.statistics().maximumLength());
        assertEquals(1_250, game.statistics().playTimeMillis());
        assertEquals(30.0, game.statistics().averagePointsPerFood());
    }

    @Test
    void appliesFreshnessMultiplierAsFoodAges() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)), new Random(1));

        game.advanceTime(3_000);
        assertEquals(2, game.scoreMultiplier());
        game.tick();

        assertEquals(Game.POINTS_PER_FOOD * 2, game.score());
    }

    @Test
    void bonusFoodAwardsTripleBasePointsBeforeFreshnessMultiplier() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Food bonus = new Food(new Position(5, 3), FoodType.BONUS);
        Game game = new Game(8, 6, snake, bonus, new Random(1));

        game.tick();

        assertEquals(90, game.score());
    }

    @Test
    void expiredFoodIsReplacedAndItsLifetimeResets() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Food original = new Food(new Position(7, 5));
        Game game = new Game(8, 6, snake, original, new Random(1));

        game.advanceTime(Game.FOOD_LIFETIME_MILLIS);

        assertNotSame(original, game.food());
        assertEquals(Game.FOOD_LIFETIME_MILLIS, game.foodRemainingMillis());
    }

    @Test
    void pausedGameDoesNotAgeFood() {
        Game game = new Game(8, 6, new Random(1));
        game.togglePause();

        game.advanceTime(Game.FOOD_LIFETIME_MILLIS);

        assertEquals(Game.FOOD_LIFETIME_MILLIS, game.foodRemainingMillis());
        assertEquals(0, game.statistics().playTimeMillis());
    }

    @Test
    void rejectsNegativeElapsedTime() {
        Game game = new Game(8, 6, new Random(1));

        assertThrows(IllegalArgumentException.class, () -> game.advanceTime(-1));
    }

    @Test
    void spawnsBonusFoodAtTheConfiguredProbabilityBoundary() {
        Random bonusRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };

        Game game = new Game(8, 6, bonusRandom);

        assertEquals(FoodType.BONUS, game.food().type());
    }

    @Test
    void wrapsAcrossBoardEdgesWhenEnabled() {
        Snake snake = Snake.of(List.of(
                new Position(7, 2), new Position(6, 2), new Position(5, 2), new Position(4, 2)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(3, 5)), new Random(1), true);

        game.tick();

        assertEquals(GameStatus.RUNNING, game.status());
        assertEquals(new Position(0, 2), game.snake().head());
    }
}
