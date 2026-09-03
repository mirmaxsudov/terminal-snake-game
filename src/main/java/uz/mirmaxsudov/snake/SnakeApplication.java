package uz.mirmaxsudov.snake;

import uz.mirmaxsudov.snake.game.Difficulty;
import uz.mirmaxsudov.snake.game.BoardSize;
import uz.mirmaxsudov.snake.game.Direction;
import uz.mirmaxsudov.snake.game.Game;
import uz.mirmaxsudov.snake.game.GameStatus;
import uz.mirmaxsudov.snake.storage.HighScoreStore;
import uz.mirmaxsudov.snake.terminal.InputEvent;
import uz.mirmaxsudov.snake.terminal.InputHandler;
import uz.mirmaxsudov.snake.terminal.Renderer;
import uz.mirmaxsudov.snake.terminal.TerminalManager;
import uz.mirmaxsudov.snake.terminal.TerminalSize;
import uz.mirmaxsudov.snake.ui.screen.GameScreen;
import uz.mirmaxsudov.snake.ui.screen.ConfirmationScreen;
import uz.mirmaxsudov.snake.ui.screen.HelpScreen;
import uz.mirmaxsudov.snake.ui.MainMenu;
import uz.mirmaxsudov.snake.ui.screen.ResizeScreen;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.SnakeStyle;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public final class SnakeApplication {
    private static final int MIN_COLUMNS = 60;
    private static final int MIN_ROWS = 24;
    private static final long INPUT_POLL_MILLIS = 20;
    private static final long COUNTDOWN_NANOS = 3_000_000_000L;

    private final TerminalManager terminal;
    private final HighScoreStore highScoreStore;
    private final InputHandler input;
    private final Renderer renderer;

    private final MainMenu mainMenu = new MainMenu();
    private final GameScreen gameScreen = new GameScreen();
    private final HelpScreen helpScreen = new HelpScreen();
    private final ResizeScreen resizeScreen = new ResizeScreen();
    private final ConfirmationScreen confirmationScreen = new ConfirmationScreen();

    private Difficulty difficulty = Difficulty.NORMAL;
    private BoardSize boardSize = BoardSize.MEDIUM;
    private SnakeStyle snakeStyle = SnakeStyle.BLOCKS;
    private Theme theme = Theme.DEFAULT;
    private boolean wrapWalls;
    private int highScore;

    public SnakeApplication(TerminalManager terminal, HighScoreStore highScoreStore) {
        this.terminal = terminal;
        this.highScoreStore = highScoreStore;
        this.input = new InputHandler(terminal.reader());
        this.renderer = new Renderer(terminal.writer());
        this.highScore = highScoreStore.load();
    }

    public void run() throws IOException {
        int selected = 0;
        while (true) {
            TerminalSize size = terminal.size();

            if (!size.atLeast(MIN_COLUMNS, MIN_ROWS)) {
                renderer.render(resizeScreen.render(size, theme, MIN_COLUMNS, MIN_ROWS), size);
                if (input.poll(100) == InputEvent.QUIT) return;
                continue;
            }

            renderer.render(mainMenu.render(selected, difficulty, boardSize, snakeStyle, theme,
                    highScore, wrapWalls), size);
            InputEvent event = input.poll(100);
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return;
                renderer.invalidate();
                continue;
            }
            if (event == InputEvent.UP) selected = Math.floorMod(selected - 1, MainMenu.ITEM_COUNT);
            if (event == InputEvent.DOWN) selected = (selected + 1) % MainMenu.ITEM_COUNT;
            if (event == InputEvent.ENTER) {
                switch (selected) {
                    case 0 -> {
                        if (playGame() == ScreenResult.EXIT) return;
                        renderer.invalidate();
                    }
                    case 1 -> difficulty = difficulty.next();
                    case 2 -> boardSize = boardSize.next();
                    case 3 -> snakeStyle = snakeStyle.next();
                    case 4 -> theme = theme.next();
                    case 5 -> wrapWalls = !wrapWalls;
                    case 6 -> {
                        if (showHelp() == ScreenResult.EXIT) return;
                        renderer.invalidate();
                    }
                    case 7 -> {
                        if (confirm("EXIT GAME", "Exit Java Snake?")) return;
                        renderer.invalidate();
                    }
                    default -> throw new IllegalStateException("Unknown menu item");
                }
            }
        }
    }

    private ScreenResult showHelp() throws IOException {
        while (true) {
            TerminalSize size = terminal.size();
            if (!size.atLeast(MIN_COLUMNS, MIN_ROWS)) {
                renderer.render(resizeScreen.render(size, theme, MIN_COLUMNS, MIN_ROWS), size);
            } else {
                renderer.render(helpScreen.render(theme), size);
            }
            InputEvent event = input.poll(100);
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return ScreenResult.EXIT;
                renderer.invalidate();
            }
            if (event == InputEvent.BACK || event == InputEvent.ENTER || event == InputEvent.MENU) {
                return ScreenResult.MENU;
            }
        }
    }

    private ScreenResult playGame() throws IOException {
        boolean restart;
        do {
            restart = false;
            Game game = createGame();
            int scoreAtStart = highScore;
            boolean scoreSaved = false;
            ScreenResult countdownResult = showCountdown(game);
            if (countdownResult != ScreenResult.CONTINUE) return countdownResult;
            long nextTick = System.nanoTime() + tickNanos(game.score());
            long lastTimeUpdate = System.nanoTime();

            while (true) {
                TerminalSize size = terminal.size();
                int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
                int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
                if (!size.atLeast(requiredColumns, requiredRows)) {
                    renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                    InputEvent resizeEvent = input.poll(100);
                    if (resizeEvent == InputEvent.QUIT || resizeEvent == InputEvent.MENU
                            || resizeEvent == InputEvent.BACK) {
                        boolean exit = resizeEvent == InputEvent.QUIT;
                        boolean confirmed = confirm(exit ? "EXIT GAME" : "MAIN MENU",
                                exit ? "Exit Java Snake?" : "Leave the current game?");
                        renderer.invalidate();
                        if (confirmed) return exit ? ScreenResult.EXIT : ScreenResult.MENU;
                    }
                    nextTick = System.nanoTime() + tickNanos(game.score());
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }

                boolean newHighScore = game.score() > scoreAtStart;
                boolean pulse = (System.nanoTime() / 250_000_000L) % 2 == 0;
                renderer.render(gameScreen.render(game, theme, difficulty.label(), highScore,
                        newHighScore, pulse, 0, snakeStyle), size);

                InputEvent event = input.poll(INPUT_POLL_MILLIS);
                if (event == InputEvent.QUIT || event == InputEvent.MENU || event == InputEvent.BACK) {
                    boolean exit = event == InputEvent.QUIT;
                    boolean confirmed = confirm(exit ? "EXIT GAME" : "MAIN MENU",
                            exit ? "Exit Java Snake?" : "Leave the current game?");
                    renderer.invalidate();
                    if (confirmed) return exit ? ScreenResult.EXIT : ScreenResult.MENU;
                    nextTick = System.nanoTime() + tickNanos(game.score());
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }
                long now = System.nanoTime();
                if (game.status() == GameStatus.RUNNING) {
                    game.advanceTime((now - lastTimeUpdate) / 1_000_000L);
                }
                lastTimeUpdate = now;
                if (event == InputEvent.PAUSE) {
                    game.togglePause();
                    nextTick = System.nanoTime() + tickNanos(game.score());
                }
                requestDirection(game, event);

                if (game.status() == GameStatus.GAME_OVER || game.status() == GameStatus.WON) {
                    if (!scoreSaved) {
                        highScore = Math.max(highScore, game.score());
                        if (highScore > scoreAtStart) highScoreStore.save(highScore);
                        scoreSaved = true;
                    }
                    if (event == InputEvent.RESTART) {
                        restart = true;
                        break;
                    }
                    continue;
                }

                if (game.status() == GameStatus.PAUSED) {
                    nextTick = now + tickNanos(game.score());
                } else if (now >= nextTick) {
                    game.tick();
                    long interval = tickNanos(game.score());
                    nextTick += interval;
                    if (now - nextTick > interval * 2) nextTick = now + interval;
                }
            }
        } while (restart);
        return ScreenResult.MENU;
    }

    private ScreenResult showCountdown(Game game) throws IOException {
        long remainingNanos = COUNTDOWN_NANOS;
        while (remainingNanos > 0) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (!size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                InputEvent resizeEvent = input.poll(100);
                if (resizeEvent == InputEvent.QUIT) {
                    if (confirm("EXIT GAME", "Exit Java Snake?")) return ScreenResult.EXIT;
                    renderer.invalidate();
                }
                if (resizeEvent == InputEvent.MENU || resizeEvent == InputEvent.BACK) {
                    if (confirm("MAIN MENU", "Cancel this game?")) return ScreenResult.MENU;
                    renderer.invalidate();
                }
                continue;
            }

            int seconds = Math.max(1, (int) Math.ceil(remainingNanos / 1_000_000_000.0));
            boolean pulse = (System.nanoTime() / 250_000_000L) % 2 == 0;
            renderer.render(gameScreen.render(game, theme, difficulty.label(), highScore,
                    false, pulse, seconds, snakeStyle), size);

            long beforePoll = System.nanoTime();
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            long afterPoll = System.nanoTime();
            remainingNanos -= afterPoll - beforePoll;
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return ScreenResult.EXIT;
                renderer.invalidate();
            }
            if (event == InputEvent.MENU || event == InputEvent.BACK) {
                if (confirm("MAIN MENU", "Cancel this game?")) return ScreenResult.MENU;
                renderer.invalidate();
            }
            requestDirection(game, event);
        }
        return showGo(game);
    }

    private ScreenResult showGo(Game game) throws IOException {
        long endNanos = System.nanoTime() + 400_000_000L;
        while (System.nanoTime() < endNanos) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(gameScreen.render(game, theme, difficulty.label(), highScore,
                        false, true, -1, snakeStyle), size);
            } else {
                renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
            }
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return ScreenResult.EXIT;
                renderer.invalidate();
                endNanos = System.nanoTime() + 400_000_000L;
            }
            if (event == InputEvent.MENU || event == InputEvent.BACK) {
                if (confirm("MAIN MENU", "Cancel this game?")) return ScreenResult.MENU;
                renderer.invalidate();
                endNanos = System.nanoTime() + 400_000_000L;
            }
            requestDirection(game, event);
        }
        return ScreenResult.CONTINUE;
    }

    private boolean confirm(String title, String message) throws IOException {
        while (true) {
            TerminalSize size = terminal.size();
            if (!size.atLeast(MIN_COLUMNS, MIN_ROWS)) {
                renderer.render(resizeScreen.render(size, theme, MIN_COLUMNS, MIN_ROWS), size);
            } else {
                renderer.render(confirmationScreen.render(theme, title, message), size);
            }

            InputEvent event = input.poll(100);
            if (event == InputEvent.YES) return true;
            if (event == InputEvent.NO || event == InputEvent.BACK || event == InputEvent.ENTER) {
                return false;
            }
        }
    }

    private Game createGame() {
        return new Game(boardSize.width(), boardSize.height(), ThreadLocalRandom.current(), wrapWalls);
    }

    private long tickNanos(int score) {
        long acceleration = (score / 50L) * 3L;
        return Math.max(45L, difficulty.tickMillis() - acceleration) * 1_000_000L;
    }

    private void requestDirection(Game game, InputEvent event) {
        switch (event) {
            case UP -> game.requestDirection(Direction.UP);
            case DOWN -> game.requestDirection(Direction.DOWN);
            case LEFT -> game.requestDirection(Direction.LEFT);
            case RIGHT -> game.requestDirection(Direction.RIGHT);
            default -> {
            }
        }
    }

    private enum ScreenResult {
        CONTINUE, MENU, EXIT
    }
}
