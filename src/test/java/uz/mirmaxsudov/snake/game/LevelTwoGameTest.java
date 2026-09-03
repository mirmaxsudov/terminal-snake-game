package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTwoGameTest {
    @Test
    void seededObstacleLayoutIsSafeAndDeterministic() {
        Game first = new Game(20, 12, new Random(42), false, true);
        Game second = new Game(20, 12, new Random(42), false, true);

        assertEquals(first.obstacles(), second.obstacles());
        assertEquals(first.food(), second.food());
        assertEquals(3, first.obstacles().size());
        assertTrue(first.obstacles().stream().noneMatch(first.snake()::occupies));
        assertFalse(first.obstacles().contains(first.food().position()));
    }

    @ParameterizedTest
    @CsvSource({"0,NORMAL", "55,BONUS", "70,SPEED", "80,SLOW", "90,SHRINK", "95,POISON"})
    void supportsAllFoodTypes(int roll, FoodType expected) {
        Random random = new SequenceRandom(roll, 0);

        Game game = new Game(8, 6, random);

        assertEquals(expected, game.food().type());
    }

    @Test
    void comboScoringAdvancesProgressiveLevels() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Random random = new SequenceRandom(
                99, 0, 25,
                99, 0, 25,
                99, 0, 32,
                99, 0, 0);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)), random);

        game.tick();
        game.tick();
        game.tick();
        game.requestDirection(Direction.DOWN);
        game.tick();

        assertEquals(4, game.combo());
        assertEquals(2, game.comboMultiplier());
        assertEquals(180, game.score());
        assertEquals(2, game.level());
        assertEquals(4, game.statistics().maximumCombo());
    }

    @Test
    void powerUpsCanSpawnAfterFoodIsCollected() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)),
                new SequenceRandom(0, 0, 0, 0, 0));

        game.tick();

        assertNotNull(game.powerUp());
        assertEquals(PowerUpType.SHIELD, game.powerUp().type());
    }

    @Test
    void collectedShieldAbsorbsOneCollision() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)),
                new SequenceRandom(0, 0, 0, 0, 0));
        game.tick();
        game.requestDirection(Direction.UP);
        game.tick();
        game.tick();
        game.tick();
        game.requestDirection(Direction.LEFT);
        for (int i = 0; i < 5; i++) game.tick();

        assertTrue(game.hasEffect(PowerUpType.SHIELD));
        game.tick();

        assertEquals(GameStatus.RUNNING, game.status());
        assertFalse(game.hasEffect(PowerUpType.SHIELD));
        assertEquals(new Position(0, 0), game.snake().head());
    }

    @Test
    void comboExpiresOutsideItsTimeWindow() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake, new Food(new Position(5, 3)),
                new SequenceRandom(99, 0, 0));
        game.tick();

        game.advanceTime(Game.COMBO_WINDOW_MILLIS);

        assertEquals(0, game.combo());
        assertEquals(1, game.comboMultiplier());
    }

    @Test
    void specialFoodAppliesItsGameplayEffect() {
        Snake snake = Snake.of(List.of(
                new Position(4, 3), new Position(3, 3), new Position(2, 3), new Position(1, 3)),
                Direction.RIGHT);
        Game game = new Game(8, 6, snake,
                new Food(new Position(5, 3), FoodType.SPEED), new SequenceRandom(99, 0, 0));

        game.tick();

        assertEquals(0.75, game.speedFactor());
        assertEquals(5, game.snake().length());
    }

    private static final class SequenceRandom extends Random {
        private final Queue<Integer> values;

        private SequenceRandom(Integer... values) {
            this.values = new ArrayDeque<>(Arrays.asList(values));
        }

        @Override
        public int nextInt(int bound) {
            if (values.isEmpty()) return 0;
            return Math.floorMod(values.remove(), bound);
        }
    }
}
