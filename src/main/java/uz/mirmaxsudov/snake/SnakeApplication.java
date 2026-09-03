package uz.mirmaxsudov.snake;

import uz.mirmaxsudov.snake.game.Achievement;
import uz.mirmaxsudov.snake.game.AiDifficulty;
import uz.mirmaxsudov.snake.game.BoardSize;
import uz.mirmaxsudov.snake.game.Difficulty;
import uz.mirmaxsudov.snake.game.Direction;
import uz.mirmaxsudov.snake.game.Game;
import uz.mirmaxsudov.snake.game.GameStatus;
import uz.mirmaxsudov.snake.game.GameMode;
import uz.mirmaxsudov.snake.game.MatchStatus;
import uz.mirmaxsudov.snake.game.ReplayData;
import uz.mirmaxsudov.snake.game.ReplayFrame;
import uz.mirmaxsudov.snake.game.VersusGame;
import uz.mirmaxsudov.snake.storage.AchievementStore;
import uz.mirmaxsudov.snake.storage.HighScoreStore;
import uz.mirmaxsudov.snake.storage.LeaderboardEntry;
import uz.mirmaxsudov.snake.storage.LeaderboardStore;
import uz.mirmaxsudov.snake.terminal.InputEvent;
import uz.mirmaxsudov.snake.terminal.InputHandler;
import uz.mirmaxsudov.snake.terminal.Renderer;
import uz.mirmaxsudov.snake.terminal.TerminalManager;
import uz.mirmaxsudov.snake.terminal.TerminalSize;
import uz.mirmaxsudov.snake.ui.MainMenu;
import uz.mirmaxsudov.snake.ui.SnakeStyle;
import uz.mirmaxsudov.snake.ui.Theme;
import uz.mirmaxsudov.snake.ui.screen.AchievementScreen;
import uz.mirmaxsudov.snake.ui.screen.ConfirmationScreen;
import uz.mirmaxsudov.snake.ui.screen.GameScreen;
import uz.mirmaxsudov.snake.ui.screen.HelpScreen;
import uz.mirmaxsudov.snake.ui.screen.LeaderboardScreen;
import uz.mirmaxsudov.snake.ui.screen.ResizeScreen;
import uz.mirmaxsudov.snake.ui.screen.SettingsScreen;
import uz.mirmaxsudov.snake.ui.screen.TextInputScreen;
import uz.mirmaxsudov.snake.ui.screen.VersusScreen;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class SnakeApplication {
    private static final int MIN_COLUMNS = 60;
    private static final int MIN_ROWS = 24;
    private static final long INPUT_POLL_MILLIS = 20;
    private static final long COUNTDOWN_NANOS = 3_000_000_000L;

    private final TerminalManager terminal;
    private final HighScoreStore highScoreStore;
    private final LeaderboardStore leaderboardStore;
    private final AchievementStore achievementStore;
    private final InputHandler input;
    private final Renderer renderer;
    private final MainMenu mainMenu = new MainMenu();
    private final SettingsScreen settingsScreen = new SettingsScreen();
    private final GameScreen gameScreen = new GameScreen();
    private final HelpScreen helpScreen = new HelpScreen();
    private final ResizeScreen resizeScreen = new ResizeScreen();
    private final ConfirmationScreen confirmationScreen = new ConfirmationScreen();
    private final LeaderboardScreen leaderboardScreen = new LeaderboardScreen();
    private final AchievementScreen achievementScreen = new AchievementScreen();
    private final TextInputScreen textInputScreen = new TextInputScreen();
    private final VersusScreen versusScreen = new VersusScreen();

    private Difficulty difficulty = Difficulty.NORMAL;
    private BoardSize boardSize = BoardSize.MEDIUM;
    private SnakeStyle snakeStyle = SnakeStyle.BLOCKS;
    private Theme theme = Theme.DEFAULT;
    private GameMode gameMode = GameMode.SOLO;
    private AiDifficulty aiDifficulty = AiDifficulty.STRATEGIC;
    private boolean wrapWalls;
    private boolean obstaclesEnabled = true;
    private String playerName = "PLAYER";
    private Long configuredSeed;
    private ReplayData lastReplay;
    private Set<Achievement> achievements;
    private int highScore;

    public SnakeApplication(TerminalManager terminal, HighScoreStore highScoreStore) {
        this(terminal, highScoreStore, LeaderboardStore.defaultStore(),
                AchievementStore.defaultStore());
    }

    SnakeApplication(TerminalManager terminal, HighScoreStore highScoreStore,
                     LeaderboardStore leaderboardStore, AchievementStore achievementStore) {
        this.terminal = terminal;
        this.highScoreStore = highScoreStore;
        this.leaderboardStore = leaderboardStore;
        this.achievementStore = achievementStore;
        this.input = new InputHandler(terminal.reader());
        this.renderer = new Renderer(terminal.writer());
        this.highScore = highScoreStore.load();
        Set<Achievement> storedAchievements = achievementStore.load();
        this.achievements = storedAchievements.isEmpty() ? EnumSet.noneOf(Achievement.class)
                : EnumSet.copyOf(storedAchievements);
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

            renderer.render(mainMenu.render(selected, theme, highScore, lastReplay != null,
                    gameMode), size);
            InputEvent event = input.poll(100);
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return;
                renderer.invalidate();
                continue;
            }
            if (isUp(event)) selected = Math.floorMod(selected - 1, MainMenu.ITEM_COUNT);
            if (isDown(event)) selected = (selected + 1) % MainMenu.ITEM_COUNT;
            if (event != InputEvent.ENTER) continue;

            switch (selected) {
                case 0 -> {
                    if (playGame() == ScreenResult.EXIT) return;
                    renderer.invalidate();
                }
                case 1 -> {
                    if (showSettings() == ScreenResult.EXIT) return;
                    renderer.invalidate();
                }
                case 2 -> { showLeaderboard(); renderer.invalidate(); }
                case 3 -> { showAchievements(); renderer.invalidate(); }
                case 4 -> {
                    if (lastReplay != null && playReplay(lastReplay) == ScreenResult.EXIT) return;
                    renderer.invalidate();
                }
                case 5 -> {
                    if (showHelp() == ScreenResult.EXIT) return;
                    renderer.invalidate();
                }
                case 6 -> {
                    if (confirm("EXIT GAME", "Exit Java Snake?")) return;
                    renderer.invalidate();
                }
                default -> throw new IllegalStateException("Unknown menu item");
            }
        }
    }

    private ScreenResult showSettings() throws IOException {
        int selected = 0;
        while (true) {
            TerminalSize size = terminal.size();
            renderer.render(settingsScreen.render(selected, difficulty, boardSize, snakeStyle,
                    theme, wrapWalls, obstaclesEnabled, playerName, configuredSeed, gameMode,
                    aiDifficulty), size);
            InputEvent event = input.poll(100);
            if (event == InputEvent.QUIT) {
                if (confirm("EXIT GAME", "Exit Java Snake?")) return ScreenResult.EXIT;
                renderer.invalidate();
            }
            if (event == InputEvent.BACK || event == InputEvent.MENU) return ScreenResult.MENU;
            if (isUp(event)) selected = Math.floorMod(selected - 1, SettingsScreen.ITEM_COUNT);
            if (isDown(event)) selected = (selected + 1) % SettingsScreen.ITEM_COUNT;
            if (event != InputEvent.ENTER) continue;
            switch (selected) {
                case 0 -> playerName = editText("PLAYER NAME", playerName,
                        "Letters, numbers, _ and -", 12, false);
                case 1 -> gameMode = gameMode.next();
                case 2 -> aiDifficulty = aiDifficulty.next();
                case 3 -> difficulty = difficulty.next();
                case 4 -> boardSize = boardSize.next();
                case 5 -> snakeStyle = snakeStyle.next();
                case 6 -> theme = theme.next();
                case 7 -> wrapWalls = !wrapWalls;
                case 8 -> obstaclesEnabled = !obstaclesEnabled;
                case 9 -> editSeed();
                case 10 -> { return ScreenResult.MENU; }
                default -> throw new IllegalStateException("Unknown setting");
            }
            renderer.invalidate();
        }
    }

    private void editSeed() throws IOException {
        String initial = configuredSeed == null ? "" : configuredSeed.toString();
        String value = editText("GAME SEED", initial, "Empty means random", 19, true);
        if (value.isBlank()) configuredSeed = null;
        else {
            try { configuredSeed = Long.parseLong(value); }
            catch (NumberFormatException ignored) { }
        }
    }

    private String editText(String title, String initial, String hint, int maxLength,
                            boolean numeric) throws IOException {
        StringBuilder value = new StringBuilder(initial);
        while (true) {
            TerminalSize size = terminal.size();
            renderer.render(textInputScreen.render(theme, title, value.toString(), hint), size);
            int character = input.readCharacter(100);
            if (character < 0) continue;
            if (character == 27 || character == 3) return initial;
            if (character == '\r' || character == '\n') {
                String result = value.toString().strip();
                return numeric || !result.isBlank() ? result : initial;
            }
            if (character == 8 || character == 127) {
                if (!value.isEmpty()) value.deleteCharAt(value.length() - 1);
                continue;
            }
            if (value.length() >= maxLength) continue;
            char typed = (char) character;
            boolean allowed = numeric ? Character.isDigit(typed) || (typed == '-' && value.isEmpty())
                    : Character.isLetterOrDigit(typed) || typed == '_' || typed == '-';
            if (allowed) value.append(typed);
        }
    }

    private ScreenResult playGame() throws IOException {
        return gameMode == GameMode.SOLO ? playSoloGame() : playVersusGame();
    }

    private ScreenResult playSoloGame() throws IOException {
        boolean restart;
        do {
            restart = false;
            long seed = configuredSeed != null ? configuredSeed : ThreadLocalRandom.current().nextLong();
            Game game = createGame(seed);
            List<ReplayFrame> replayFrames = new ArrayList<>();
            long elapsedSinceTick = 0;
            int scoreAtStart = highScore;
            boolean resultSaved = false;
            ScreenResult countdownResult = showCountdown(game);
            if (countdownResult != ScreenResult.CONTINUE) return countdownResult;
            long nextTick = System.nanoTime() + tickNanos(game);
            long lastTimeUpdate = System.nanoTime();

            while (true) {
                TerminalSize size = terminal.size();
                int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
                int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
                if (!size.atLeast(requiredColumns, requiredRows)) {
                    renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                    InputEvent resizeEvent = input.poll(100);
                    ScreenResult navigation = confirmNavigation(resizeEvent);
                    if (navigation != ScreenResult.CONTINUE) return navigation;
                    nextTick = System.nanoTime() + tickNanos(game);
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }

                boolean pulse = (System.nanoTime() / 250_000_000L) % 2 == 0;
                renderer.render(gameScreen.render(game, theme, difficulty.label(), highScore,
                        game.score() > scoreAtStart, pulse, 0, snakeStyle), size);
                InputEvent event = input.poll(INPUT_POLL_MILLIS);
                ScreenResult navigation = confirmNavigation(event);
                if (navigation != ScreenResult.CONTINUE) return navigation;
                if (event == InputEvent.QUIT || event == InputEvent.MENU || event == InputEvent.BACK) {
                    nextTick = System.nanoTime() + tickNanos(game);
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }

                long now = System.nanoTime();
                if (game.status() == GameStatus.RUNNING) {
                    long elapsed = (now - lastTimeUpdate) / 1_000_000L;
                    game.advanceTime(elapsed);
                    elapsedSinceTick += elapsed;
                }
                lastTimeUpdate = now;
                if (event == InputEvent.PAUSE) {
                    game.togglePause();
                    nextTick = now + tickNanos(game);
                }
                requestDirection(game, event);

                if (game.status() == GameStatus.GAME_OVER || game.status() == GameStatus.WON) {
                    if (!resultSaved) {
                        lastReplay = replay(seed, replayFrames);
                        saveResult(game);
                        resultSaved = true;
                    }
                    if (event == InputEvent.RESTART) { restart = true; break; }
                    continue;
                }

                if (game.status() == GameStatus.PAUSED) {
                    nextTick = now + tickNanos(game);
                } else if (now >= nextTick) {
                    Direction direction = game.queuedDirection();
                    game.tick();
                    replayFrames.add(new ReplayFrame(direction, elapsedSinceTick));
                    elapsedSinceTick = 0;
                    if ((game.status() == GameStatus.GAME_OVER || game.status() == GameStatus.WON)
                            && !resultSaved) {
                        lastReplay = replay(seed, replayFrames);
                        saveResult(game);
                        resultSaved = true;
                    }
                    long interval = tickNanos(game);
                    nextTick += interval;
                    if (now - nextTick > interval * 2) nextTick = now + interval;
                }
            }
        } while (restart);
        return ScreenResult.MENU;
    }

    private ScreenResult playVersusGame() throws IOException {
        boolean restart;
        do {
            restart = false;
            long seed = configuredSeed != null ? configuredSeed : ThreadLocalRandom.current().nextLong();
            VersusGame game = new VersusGame(boardSize.width(), boardSize.height(),
                    new Random(seed), wrapWalls, obstaclesEnabled, gameMode, aiDifficulty);
            ScreenResult countdownResult = showVersusCountdown(game);
            if (countdownResult != ScreenResult.CONTINUE) return countdownResult;
            long nextTick = System.nanoTime() + versusTickNanos();
            long lastTimeUpdate = System.nanoTime();

            while (true) {
                TerminalSize size = terminal.size();
                int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
                int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
                if (!size.atLeast(requiredColumns, requiredRows)) {
                    renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                    ScreenResult navigation = confirmNavigation(input.poll(100));
                    if (navigation != ScreenResult.CONTINUE) return navigation;
                    nextTick = System.nanoTime() + versusTickNanos();
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }

                boolean pulse = (System.nanoTime() / 250_000_000L) % 2 == 0;
                renderer.render(versusScreen.render(game, theme, aiDifficulty, snakeStyle,
                        pulse, 0), size);
                InputEvent event = input.poll(INPUT_POLL_MILLIS);
                ScreenResult navigation = confirmNavigation(event);
                if (navigation != ScreenResult.CONTINUE) return navigation;
                if (event == InputEvent.QUIT || event == InputEvent.MENU || event == InputEvent.BACK) {
                    nextTick = System.nanoTime() + versusTickNanos();
                    lastTimeUpdate = System.nanoTime();
                    continue;
                }

                long now = System.nanoTime();
                if (game.status() == MatchStatus.RUNNING) {
                    game.advanceTime((now - lastTimeUpdate) / 1_000_000L);
                }
                lastTimeUpdate = now;
                if (event == InputEvent.PAUSE) {
                    game.togglePause();
                    nextTick = now + versusTickNanos();
                }
                requestVersusDirection(game, event);

                if (game.status().finished()) {
                    if (event == InputEvent.RESTART) {
                        restart = true;
                        break;
                    }
                    continue;
                }
                if (game.status() == MatchStatus.PAUSED) {
                    nextTick = now + versusTickNanos();
                } else if (now >= nextTick) {
                    game.tick();
                    long interval = versusTickNanos();
                    nextTick += interval;
                    if (now - nextTick > interval * 2) nextTick = now + interval;
                }
            }
        } while (restart);
        return ScreenResult.MENU;
    }

    private long versusTickNanos() {
        return Math.max(45L, difficulty.tickMillis()) * 1_000_000L;
    }

    private ScreenResult showVersusCountdown(VersusGame game) throws IOException {
        long remainingNanos = COUNTDOWN_NANOS;
        while (remainingNanos > 0) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (!size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                ScreenResult navigation = confirmNavigation(input.poll(100));
                if (navigation != ScreenResult.CONTINUE) return navigation;
                continue;
            }
            int seconds = Math.max(1, (int) Math.ceil(remainingNanos / 1_000_000_000.0));
            renderer.render(versusScreen.render(game, theme, aiDifficulty, snakeStyle,
                    true, seconds), size);
            long beforePoll = System.nanoTime();
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            remainingNanos -= System.nanoTime() - beforePoll;
            ScreenResult navigation = confirmNavigation(event);
            if (navigation != ScreenResult.CONTINUE) return navigation;
            requestVersusDirection(game, event);
        }

        long endNanos = System.nanoTime() + 400_000_000L;
        while (System.nanoTime() < endNanos) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(versusScreen.render(game, theme, aiDifficulty, snakeStyle,
                        true, -1), size);
            } else renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            ScreenResult navigation = confirmNavigation(event);
            if (navigation != ScreenResult.CONTINUE) return navigation;
            requestVersusDirection(game, event);
        }
        return ScreenResult.CONTINUE;
    }

    private ReplayData replay(long seed, List<ReplayFrame> frames) {
        return new ReplayData(seed, boardSize.width(), boardSize.height(), wrapWalls,
                obstaclesEnabled, difficulty, frames);
    }

    private void saveResult(Game game) {
        highScore = Math.max(highScore, game.score());
        highScoreStore.save(highScore);
        leaderboardStore.add(new LeaderboardEntry(playerName, game.score(), difficulty.label(),
                game.level(), LocalDate.now()));
        Set<Achievement> updated = EnumSet.copyOf(achievements.isEmpty()
                ? EnumSet.noneOf(Achievement.class) : achievements);
        for (Achievement achievement : Achievement.values()) {
            if (achievement.earned(game)) updated.add(achievement);
        }
        achievements = updated;
        achievementStore.save(achievements);
    }

    private ScreenResult playReplay(ReplayData replay) throws IOException {
        Game game = new Game(replay.width(), replay.height(), new Random(replay.seed()),
                replay.wrapWalls(), replay.obstaclesEnabled());
        int frameIndex = 0;
        long nextTick = System.nanoTime();
        while (true) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (!size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
            } else {
                renderer.render(gameScreen.render(game, theme, "Replay", highScore,
                        false, true, 0, snakeStyle), size);
            }
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            if (event == InputEvent.QUIT) return ScreenResult.EXIT;
            if (event == InputEvent.MENU || event == InputEvent.BACK || event == InputEvent.ENTER) {
                return ScreenResult.MENU;
            }
            long now = System.nanoTime();
            if (frameIndex >= replay.frames().size()) continue;
            if (now >= nextTick) {
                ReplayFrame frame = replay.frames().get(frameIndex++);
                game.advanceTime(frame.elapsedMillis());
                game.requestDirection(frame.direction());
                game.tick();
                nextTick = now + replay.difficulty().tickMillis() * 1_000_000L;
            }
        }
    }

    private Game createGame(long seed) {
        return new Game(boardSize.width(), boardSize.height(), new Random(seed), wrapWalls,
                obstaclesEnabled);
    }

    private long tickNanos(Game game) {
        long acceleration = (game.score() / 50L) * 3L + (game.level() - 1L) * 4L;
        long base = Math.max(40L, difficulty.tickMillis() - acceleration);
        return Math.max(30L, Math.round(base * game.speedFactor())) * 1_000_000L;
    }

    private ScreenResult confirmNavigation(InputEvent event) throws IOException {
        if (event != InputEvent.QUIT && event != InputEvent.MENU && event != InputEvent.BACK) {
            return ScreenResult.CONTINUE;
        }
        boolean exit = event == InputEvent.QUIT;
        boolean confirmed = confirm(exit ? "EXIT GAME" : "MAIN MENU",
                exit ? "Exit Java Snake?" : "Leave the current game?");
        renderer.invalidate();
        if (!confirmed) return ScreenResult.CONTINUE;
        return exit ? ScreenResult.EXIT : ScreenResult.MENU;
    }

    private ScreenResult showCountdown(Game game) throws IOException {
        long remainingNanos = COUNTDOWN_NANOS;
        while (remainingNanos > 0) {
            TerminalSize size = terminal.size();
            int requiredColumns = Math.max(MIN_COLUMNS, game.width() * 2 + 2);
            int requiredRows = Math.max(MIN_ROWS, game.height() + 7);
            if (!size.atLeast(requiredColumns, requiredRows)) {
                renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
                ScreenResult navigation = confirmNavigation(input.poll(100));
                if (navigation != ScreenResult.CONTINUE) return navigation;
                continue;
            }
            int seconds = Math.max(1, (int) Math.ceil(remainingNanos / 1_000_000_000.0));
            renderer.render(gameScreen.render(game, theme, difficulty.label(), highScore,
                    false, true, seconds, snakeStyle), size);
            long beforePoll = System.nanoTime();
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            remainingNanos -= System.nanoTime() - beforePoll;
            ScreenResult navigation = confirmNavigation(event);
            if (navigation != ScreenResult.CONTINUE) return navigation;
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
            } else renderer.render(resizeScreen.render(size, theme, requiredColumns, requiredRows), size);
            InputEvent event = input.poll(INPUT_POLL_MILLIS);
            ScreenResult navigation = confirmNavigation(event);
            if (navigation != ScreenResult.CONTINUE) return navigation;
            requestDirection(game, event);
        }
        return ScreenResult.CONTINUE;
    }

    private ScreenResult showHelp() throws IOException {
        while (true) {
            TerminalSize size = terminal.size();
            renderer.render(helpScreen.render(theme), size);
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

    private void showLeaderboard() throws IOException {
        while (true) {
            renderer.render(leaderboardScreen.render(theme, leaderboardStore.load()), terminal.size());
            InputEvent event = input.poll(100);
            if (event == InputEvent.BACK || event == InputEvent.ENTER || event == InputEvent.MENU
                    || event == InputEvent.QUIT) return;
        }
    }

    private void showAchievements() throws IOException {
        while (true) {
            renderer.render(achievementScreen.render(theme, achievements), terminal.size());
            InputEvent event = input.poll(100);
            if (event == InputEvent.BACK || event == InputEvent.ENTER || event == InputEvent.MENU
                    || event == InputEvent.QUIT) return;
        }
    }

    private boolean confirm(String title, String message) throws IOException {
        while (true) {
            renderer.render(confirmationScreen.render(theme, title, message), terminal.size());
            InputEvent event = input.poll(100);
            if (event == InputEvent.YES) return true;
            if (event == InputEvent.NO || event == InputEvent.BACK || event == InputEvent.ENTER) return false;
        }
    }

    private void requestDirection(Game game, InputEvent event) {
        switch (event) {
            case UP, PLAYER_TWO_UP -> game.requestDirection(Direction.UP);
            case DOWN, PLAYER_TWO_DOWN -> game.requestDirection(Direction.DOWN);
            case LEFT, PLAYER_TWO_LEFT -> game.requestDirection(Direction.LEFT);
            case RIGHT, PLAYER_TWO_RIGHT -> game.requestDirection(Direction.RIGHT);
            default -> { }
        }
    }

    private void requestVersusDirection(VersusGame game, InputEvent event) {
        switch (event) {
            case UP -> game.requestPlayerOneDirection(Direction.UP);
            case DOWN -> game.requestPlayerOneDirection(Direction.DOWN);
            case LEFT -> game.requestPlayerOneDirection(Direction.LEFT);
            case RIGHT -> game.requestPlayerOneDirection(Direction.RIGHT);
            case PLAYER_TWO_UP -> requestArrowDirection(game, Direction.UP);
            case PLAYER_TWO_DOWN -> requestArrowDirection(game, Direction.DOWN);
            case PLAYER_TWO_LEFT -> requestArrowDirection(game, Direction.LEFT);
            case PLAYER_TWO_RIGHT -> requestArrowDirection(game, Direction.RIGHT);
            default -> { }
        }
    }

    private void requestArrowDirection(VersusGame game, Direction direction) {
        if (game.mode() == GameMode.TWO_PLAYER) game.requestPlayerTwoDirection(direction);
        else game.requestPlayerOneDirection(direction);
    }

    private boolean isUp(InputEvent event) {
        return event == InputEvent.UP || event == InputEvent.PLAYER_TWO_UP;
    }

    private boolean isDown(InputEvent event) {
        return event == InputEvent.DOWN || event == InputEvent.PLAYER_TWO_DOWN;
    }

    private void requestDirection(Game game, Direction direction) { game.requestDirection(direction); }

    private enum ScreenResult { CONTINUE, MENU, EXIT }
}
