package uz.mirmaxsudov.snake.ui;

import org.junit.jupiter.api.Test;
import uz.mirmaxsudov.snake.game.Game;
import uz.mirmaxsudov.snake.game.AiDifficulty;
import uz.mirmaxsudov.snake.game.GameMode;
import uz.mirmaxsudov.snake.game.VersusGame;
import uz.mirmaxsudov.snake.terminal.Ansi;
import uz.mirmaxsudov.snake.ui.screen.ConfirmationScreen;
import uz.mirmaxsudov.snake.ui.screen.GameScreen;
import uz.mirmaxsudov.snake.ui.screen.VersusScreen;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiConfigurationTest {
    @Test
    void everySnakeStyleOccupiesOneLogicalCell() {
        for (SnakeStyle style : SnakeStyle.values()) {
            assertEquals(2, style.head().length());
            assertEquals(2, style.body().length());
        }
    }

    @Test
    void gameScreenUsesSelectedSnakeStyle() {
        Game game = new Game(20, 12, new Random(1));

        String screen = String.join("\n", new GameScreen().render(
                game, Theme.DEFAULT, "Normal", 0, false, false, 0, SnakeStyle.ASCII));

        assertTrue(screen.contains("@@"));
        assertTrue(screen.contains("##"));
    }

    @Test
    void smallBoardHudStaysWithinItsBorder() {
        Game game = new Game(20, 12, new Random(1), true);

        for (String line : new GameScreen().render(
                game, Theme.DEFAULT, "Normal", 99_999, false, false, 0, SnakeStyle.BLOCKS)) {
            assertTrue(Ansi.visibleLength(line) <= 42, "Line exceeds the 20-cell board: " + line);
        }
    }

    @Test
    void accessibilityThemesArePartOfThemeCycle() {
        assertEquals(Theme.HIGH_CONTRAST, Theme.MONOCHROME.next());
        assertEquals(Theme.COLOR_SAFE, Theme.HIGH_CONTRAST.next());
        assertEquals(Theme.DEFAULT, Theme.COLOR_SAFE.next());
    }

    @Test
    void confirmationScreenExplainsBothChoices() {
        String screen = String.join("\n", new ConfirmationScreen().render(
                Theme.DEFAULT, "EXIT GAME", "Exit Java Snake?"));

        assertTrue(screen.contains("Y Confirm"));
        assertTrue(screen.contains("N / Esc Cancel"));
    }

    @Test
    void versusScreenShowsBothControlSchemesAndStaysInsideBoard() {
        VersusGame game = new VersusGame(20, 12, new Random(1), false, true,
                GameMode.TWO_PLAYER, AiDifficulty.STRATEGIC);
        var lines = new VersusScreen().render(game, Theme.DEFAULT,
                AiDifficulty.STRATEGIC, SnakeStyle.BLOCKS, false, 0);

        assertTrue(String.join("\n", lines).contains("P1 WASD"));
        assertTrue(String.join("\n", lines).contains("P2 ARROWS"));
        for (String line : lines) {
            assertTrue(Ansi.visibleLength(line) <= 42, "Line exceeds the 20-cell board: " + line);
        }
    }

    @Test
    void gameModesAndAiDifficultiesCycle() {
        assertEquals(GameMode.VS_AI, GameMode.SOLO.next());
        assertEquals(GameMode.TWO_PLAYER, GameMode.VS_AI.next());
        assertEquals(GameMode.SOLO, GameMode.TWO_PLAYER.next());
        assertEquals(AiDifficulty.STRATEGIC, AiDifficulty.SIMPLE.next());
        assertEquals(AiDifficulty.PATHFINDER, AiDifficulty.STRATEGIC.next());
        assertEquals(AiDifficulty.SIMPLE, AiDifficulty.PATHFINDER.next());
    }
}
