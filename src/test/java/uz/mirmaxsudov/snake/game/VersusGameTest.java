package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersusGameTest {
    @Test
    void playersCompeteForFoodAndGrowIndependently() {
        Snake playerOne = snake(List.of(new Position(3, 3), new Position(2, 3),
                new Position(1, 3), new Position(0, 3)), Direction.RIGHT);
        Snake playerTwo = snake(List.of(new Position(8, 6), new Position(9, 6),
                new Position(10, 6), new Position(11, 6)), Direction.LEFT);
        VersusGame game = game(playerOne, playerTwo, new Food(new Position(4, 3)), Set.of());

        game.tick();

        assertEquals(30, game.playerOneScore());
        assertEquals(0, game.playerTwoScore());
        assertEquals(5, game.playerOne().length());
        assertEquals(4, game.playerTwo().length());
        assertEquals(MatchStatus.RUNNING, game.status());
    }

    @Test
    void simultaneousHeadOnCollisionIsADraw() {
        Snake playerOne = snake(List.of(new Position(4, 3), new Position(3, 3),
                new Position(2, 3), new Position(1, 3)), Direction.RIGHT);
        Snake playerTwo = snake(List.of(new Position(6, 3), new Position(7, 3),
                new Position(8, 3), new Position(9, 3)), Direction.LEFT);
        VersusGame game = game(playerOne, playerTwo, new Food(new Position(0, 0)), Set.of());

        game.tick();

        assertEquals(MatchStatus.DRAW, game.status());
    }

    @Test
    void onePlayersCrashAwardsTheMatchToTheOtherPlayer() {
        Snake playerOne = snake(List.of(new Position(11, 2), new Position(10, 2),
                new Position(9, 2), new Position(8, 2)), Direction.RIGHT);
        Snake playerTwo = snake(List.of(new Position(7, 6), new Position(8, 6),
                new Position(9, 6), new Position(10, 6)), Direction.LEFT);
        VersusGame game = game(playerOne, playerTwo, new Food(new Position(0, 0)), Set.of());

        game.tick();

        assertEquals(MatchStatus.PLAYER_TWO_WON, game.status());
    }

    @Test
    void localPlayersQueueTurnsIndependently() {
        VersusGame game = new VersusGame(20, 12, new Random(4), false, false,
                GameMode.TWO_PLAYER, AiDifficulty.STRATEGIC);
        Position firstHead = game.playerOne().head();
        Position secondHead = game.playerTwo().head();

        game.requestPlayerOneDirection(Direction.DOWN);
        game.requestPlayerTwoDirection(Direction.UP);
        game.tick();

        assertEquals(firstHead.translate(Direction.DOWN), game.playerOne().head());
        assertEquals(secondHead.translate(Direction.UP), game.playerTwo().head());
    }

    @Test
    void strategicAiTakesTheSafeMoveTowardFood() {
        Snake ai = snake(List.of(new Position(8, 5), new Position(9, 5),
                new Position(10, 5), new Position(11, 5)), Direction.LEFT);
        Snake opponent = snake(List.of(new Position(2, 2), new Position(1, 2),
                new Position(0, 2)), Direction.RIGHT);

        Direction choice = AiController.choose(AiDifficulty.STRATEGIC, ai, opponent,
                new Position(8, 3), Set.of(), 12, 8, false, new Random(1));

        assertEquals(Direction.UP, choice);
    }

    @Test
    void pathfinderRoutesAroundBlockedDirectPath() {
        Snake ai = snake(List.of(new Position(3, 4), new Position(2, 4),
                new Position(1, 4), new Position(0, 4)), Direction.RIGHT);
        Snake opponent = snake(List.of(new Position(9, 7), new Position(10, 7),
                new Position(11, 7)), Direction.LEFT);

        Direction choice = AiController.choose(AiDifficulty.PATHFINDER, ai, opponent,
                new Position(6, 4), Set.of(new Position(4, 4)), 12, 8, false,
                new Random(1));

        assertEquals(Direction.UP, choice);
    }

    @Test
    void casualAiNeverChoosesAnImmediateCollisionOrReverse() {
        Snake ai = snake(List.of(new Position(3, 4), new Position(2, 4),
                new Position(1, 4), new Position(0, 4)), Direction.RIGHT);
        Snake opponent = snake(List.of(new Position(9, 7), new Position(10, 7),
                new Position(11, 7)), Direction.LEFT);

        Direction choice = AiController.choose(AiDifficulty.SIMPLE, ai, opponent,
                new Position(6, 4), Set.of(new Position(4, 4), new Position(3, 3)),
                12, 8, false, new Random(2));

        assertEquals(Direction.DOWN, choice);
        assertNotEquals(Direction.LEFT, choice);
    }

    @Test
    void seededArenaIsDeterministicAndKeepsObstaclesOffBothSnakes() {
        VersusGame first = new VersusGame(20, 12, new Random(42), false, true,
                GameMode.VS_AI, AiDifficulty.PATHFINDER);
        VersusGame second = new VersusGame(20, 12, new Random(42), false, true,
                GameMode.VS_AI, AiDifficulty.PATHFINDER);

        assertEquals(first.obstacles(), second.obstacles());
        assertEquals(first.food(), second.food());
        assertTrue(first.obstacles().stream().noneMatch(first.playerOne()::occupies));
        assertTrue(first.obstacles().stream().noneMatch(first.playerTwo()::occupies));
        assertFalse(first.obstacles().contains(first.food().position()));
    }

    @Test
    void opponentBodyActsAsCollisionTerritory() {
        Snake playerOne = snake(List.of(new Position(5, 3), new Position(4, 3),
                new Position(3, 3), new Position(2, 3)), Direction.RIGHT);
        Snake playerTwo = snake(List.of(new Position(6, 2), new Position(6, 3),
                new Position(6, 4), new Position(7, 4)), Direction.UP);
        VersusGame game = game(playerOne, playerTwo, new Food(new Position(0, 0)), Set.of());

        game.tick();

        assertEquals(MatchStatus.PLAYER_TWO_WON, game.status());
    }

    private VersusGame game(Snake one, Snake two, Food food, Set<Position> obstacles) {
        return new VersusGame(12, 8, one, two, food, obstacles, new Random(1),
                false, !obstacles.isEmpty(), GameMode.TWO_PLAYER, AiDifficulty.STRATEGIC);
    }

    private Snake snake(List<Position> positions, Direction direction) {
        return Snake.of(positions, direction);
    }
}
