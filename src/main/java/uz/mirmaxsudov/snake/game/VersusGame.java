package uz.mirmaxsudov.snake.game;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class VersusGame {
    private final int width;
    private final int height;
    private final RandomGenerator random;
    private final boolean wrapWalls;
    private final boolean obstaclesEnabled;
    private final GameMode mode;
    private final AiDifficulty aiDifficulty;
    private final Snake playerOne;
    private final Snake playerTwo;
    private final Set<Position> obstacles = new LinkedHashSet<>();
    private Direction playerOneDirection;
    private Direction playerTwoDirection;
    private boolean playerOneDirectionQueued;
    private boolean playerTwoDirectionQueued;
    private Food food;
    private long foodRemainingMillis = Game.FOOD_LIFETIME_MILLIS;
    private long playTimeMillis;
    private int playerOneScore;
    private int playerTwoScore;
    private MatchStatus status = MatchStatus.RUNNING;

    public VersusGame(int width, int height, RandomGenerator random, boolean wrapWalls,
                      boolean obstaclesEnabled, GameMode mode, AiDifficulty aiDifficulty) {
        this(width, height, startingPlayerOne(width, height), startingPlayerTwo(width, height),
                null, Set.of(), random, wrapWalls, obstaclesEnabled, mode, aiDifficulty);
        if (obstaclesEnabled) addObstacles(Math.max(3, width * height / 100));
        spawnFood();
    }

    VersusGame(int width, int height, Snake playerOne, Snake playerTwo, Food food,
               Set<Position> obstacles, RandomGenerator random, boolean wrapWalls,
               boolean obstaclesEnabled, GameMode mode, AiDifficulty aiDifficulty) {
        if (width < 12 || height < 8) {
            throw new IllegalArgumentException("Versus boards must be at least 12x8");
        }
        if (mode == GameMode.SOLO) throw new IllegalArgumentException("Versus mode cannot be Solo");
        this.width = width;
        this.height = height;
        this.playerOne = Objects.requireNonNull(playerOne);
        this.playerTwo = Objects.requireNonNull(playerTwo);
        this.food = food;
        this.obstacles.addAll(Objects.requireNonNull(obstacles));
        this.random = Objects.requireNonNull(random);
        this.wrapWalls = wrapWalls;
        this.obstaclesEnabled = obstaclesEnabled;
        this.mode = Objects.requireNonNull(mode);
        this.aiDifficulty = Objects.requireNonNull(aiDifficulty);
        this.playerOneDirection = playerOne.direction();
        this.playerTwoDirection = playerTwo.direction();
    }

    private static Snake startingPlayerOne(int width, int height) {
        int x = Math.max(3, width / 4);
        int y = Math.max(1, height / 3);
        return Snake.of(List.of(new Position(x, y), new Position(x - 1, y),
                new Position(x - 2, y), new Position(x - 3, y)), Direction.RIGHT);
    }

    private static Snake startingPlayerTwo(int width, int height) {
        int x = Math.min(width - 4, width * 3 / 4);
        int y = Math.min(height - 2, height * 2 / 3);
        return Snake.of(List.of(new Position(x, y), new Position(x + 1, y),
                new Position(x + 2, y), new Position(x + 3, y)), Direction.LEFT);
    }

    public void requestPlayerOneDirection(Direction requested) {
        if (!validTurn(playerOne, requested)) return;
        if (status == MatchStatus.RUNNING) {
            if (playerOneDirectionQueued) return;
            playerOneDirectionQueued = true;
        }
        playerOneDirection = requested;
    }

    public void requestPlayerTwoDirection(Direction requested) {
        if (mode != GameMode.TWO_PLAYER) return;
        queuePlayerTwoDirection(requested);
    }

    private void queuePlayerTwoDirection(Direction requested) {
        if (!validTurn(playerTwo, requested)) return;
        if (status == MatchStatus.RUNNING) {
            if (playerTwoDirectionQueued) return;
            playerTwoDirectionQueued = true;
        }
        playerTwoDirection = requested;
    }

    private boolean validTurn(Snake snake, Direction requested) {
        Objects.requireNonNull(requested);
        return requested != snake.direction() && !requested.isOpposite(snake.direction());
    }

    public void togglePause() {
        if (status == MatchStatus.RUNNING) status = MatchStatus.PAUSED;
        else if (status == MatchStatus.PAUSED) status = MatchStatus.RUNNING;
    }

    public void advanceTime(long elapsedMillis) {
        if (elapsedMillis < 0) throw new IllegalArgumentException("Elapsed time cannot be negative");
        if (elapsedMillis == 0 || status != MatchStatus.RUNNING) return;
        playTimeMillis += elapsedMillis;
        if (food != null) {
            foodRemainingMillis -= elapsedMillis;
            if (foodRemainingMillis <= 0) spawnFood();
        }
    }

    public void tick() {
        if (status != MatchStatus.RUNNING) return;
        if (mode == GameMode.VS_AI) {
            playerTwoDirection = AiController.choose(aiDifficulty, playerTwo, playerOne,
                    food == null ? null : food.position(), obstacles, width, height, wrapWalls, random);
        }

        Position nextOne = resolve(playerOne.head().translate(playerOneDirection));
        Position nextTwo = resolve(playerTwo.head().translate(playerTwoDirection));
        boolean oneEating = food != null && food.position().equals(nextOne);
        boolean twoEating = food != null && food.position().equals(nextTwo);
        boolean oneGrowing = oneEating && food.type().growsSnake();
        boolean twoGrowing = twoEating && food.type().growsSnake();

        boolean oneCrashed = collision(playerOne, playerTwo, nextOne, oneGrowing, twoGrowing);
        boolean twoCrashed = collision(playerTwo, playerOne, nextTwo, twoGrowing, oneGrowing);
        if (nextOne != null && nextOne.equals(nextTwo)) {
            oneCrashed = true;
            twoCrashed = true;
        }
        if (nextOne != null && nextTwo != null
                && nextOne.equals(playerTwo.head()) && nextTwo.equals(playerOne.head())) {
            oneCrashed = true;
            twoCrashed = true;
        }

        playerOneDirectionQueued = false;
        playerTwoDirectionQueued = false;
        if (oneCrashed || twoCrashed) {
            status = oneCrashed && twoCrashed ? MatchStatus.DRAW
                    : oneCrashed ? MatchStatus.PLAYER_TWO_WON : MatchStatus.PLAYER_ONE_WON;
            return;
        }

        playerOne.moveTo(nextOne, playerOneDirection, oneGrowing);
        playerTwo.moveTo(nextTwo, playerTwoDirection, twoGrowing);
        if (oneEating) playerOneScore = consume(playerOne, playerOneScore);
        if (twoEating) playerTwoScore = consume(playerTwo, playerTwoScore);
        if (oneEating || twoEating) spawnFood();
    }

    private boolean collision(Snake snake, Snake opponent, Position nextHead,
                              boolean growing, boolean opponentGrowing) {
        if (nextHead == null || obstacles.contains(nextHead)) return true;
        if (occupiesAfterMove(snake, nextHead, growing)) return true;
        return occupiesAfterMove(opponent, nextHead, opponentGrowing);
    }

    private boolean occupiesAfterMove(Snake snake, Position position, boolean growing) {
        if (!snake.occupies(position)) return false;
        return growing || !snake.body().getLast().equals(position);
    }

    private int consume(Snake snake, int currentScore) {
        FoodType type = food.type();
        if (type == FoodType.SHRINK) snake.shrink(2);
        if (type.points() < 0) return Math.max(0, currentScore + type.points());
        return currentScore + type.points() * freshnessMultiplier();
    }

    private void spawnFood() {
        FoodType type = random.nextInt(100) < 20 ? FoodType.BONUS : FoodType.NORMAL;
        Optional<Position> position = freePosition();
        food = position.map(value -> new Food(value, type)).orElse(null);
        foodRemainingMillis = Game.FOOD_LIFETIME_MILLIS;
        if (food == null) {
            status = playerOneScore == playerTwoScore ? MatchStatus.DRAW
                    : playerOneScore > playerTwoScore
                    ? MatchStatus.PLAYER_ONE_WON : MatchStatus.PLAYER_TWO_WON;
        }
    }

    private void addObstacles(int count) {
        for (int i = 0; i < count; i++) freePosition().ifPresent(obstacles::add);
    }

    private Optional<Position> freePosition() {
        List<Position> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position candidate = new Position(x, y);
                if (!playerOne.occupies(candidate) && !playerTwo.occupies(candidate)
                        && !obstacles.contains(candidate)
                        && (food == null || !food.position().equals(candidate))) {
                    free.add(candidate);
                }
            }
        }
        return free.isEmpty() ? Optional.empty()
                : Optional.of(free.get(random.nextInt(free.size())));
    }

    private Position resolve(Position candidate) {
        if (wrapWalls) {
            return new Position(Math.floorMod(candidate.x(), width), Math.floorMod(candidate.y(), height));
        }
        return candidate.x() < 0 || candidate.x() >= width
                || candidate.y() < 0 || candidate.y() >= height ? null : candidate;
    }

    public int freshnessMultiplier() {
        long band = (Math.max(1, foodRemainingMillis) - 1) * 3 / Game.FOOD_LIFETIME_MILLIS;
        return band == 2 ? 3 : band == 1 ? 2 : 1;
    }

    public int width() { return width; }
    public int height() { return height; }
    public Snake playerOne() { return playerOne; }
    public Snake playerTwo() { return playerTwo; }
    public Food food() { return food; }
    public Set<Position> obstacles() { return Set.copyOf(obstacles); }
    public int playerOneScore() { return playerOneScore; }
    public int playerTwoScore() { return playerTwoScore; }
    public long foodRemainingMillis() { return foodRemainingMillis; }
    public long playTimeMillis() { return playTimeMillis; }
    public MatchStatus status() { return status; }
    public GameMode mode() { return mode; }
    public AiDifficulty aiDifficulty() { return aiDifficulty; }
    public boolean wrapWalls() { return wrapWalls; }
    public boolean obstaclesEnabled() { return obstaclesEnabled; }
    public Direction playerOneDirection() { return playerOneDirection; }
    public Direction playerTwoDirection() { return playerTwoDirection; }
}
