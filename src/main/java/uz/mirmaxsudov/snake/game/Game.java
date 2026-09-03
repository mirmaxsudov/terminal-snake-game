package uz.mirmaxsudov.snake.game;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Game {
    public static final int POINTS_PER_FOOD = 10;
    public static final long FOOD_LIFETIME_MILLIS = 9_000;
    public static final int BONUS_FOOD_PERCENT = 15;
    private final int width;
    private final int height;
    private final RandomGenerator random;
    private final Snake snake;
    private final boolean wrapWalls;
    private Food food;
    private long foodRemainingMillis = FOOD_LIFETIME_MILLIS;
    private Direction queuedDirection;
    private boolean directionQueuedThisTick;
    private int score;
    private int foodEaten;
    private int maximumLength;
    private long playTimeMillis;
    private GameStatus status = GameStatus.RUNNING;

    public Game(int width, int height, RandomGenerator random) {
        this(width, height, random, false);
    }

    public Game(int width, int height, RandomGenerator random, boolean wrapWalls) {
        this(width, height, Snake.centered(width, height), null, random, wrapWalls);
        spawnFood();
    }

    Game(int width, int height, Snake snake, Food food, RandomGenerator random) {
        this(width, height, snake, food, random, false);
    }

    Game(int width, int height, Snake snake, Food food, RandomGenerator random, boolean wrapWalls) {
        if (width < 8 || height < 6) throw new IllegalArgumentException("Board must be at least 8x6");
        this.width = width;
        this.height = height;
        this.snake = Objects.requireNonNull(snake);
        this.food = food;
        this.random = Objects.requireNonNull(random);
        this.wrapWalls = wrapWalls;
        this.queuedDirection = snake.direction();
        this.maximumLength = snake.length();
    }

    public void requestDirection(Direction requested) {
        Objects.requireNonNull(requested);
        if (!directionQueuedThisTick && requested != snake.direction()
                && !requested.isOpposite(snake.direction())) {
            queuedDirection = requested;
            directionQueuedThisTick = true;
        }
    }

    public void togglePause() {
        if (status == GameStatus.RUNNING) status = GameStatus.PAUSED;
        else if (status == GameStatus.PAUSED) status = GameStatus.RUNNING;
    }

    public void tick() {
        if (status != GameStatus.RUNNING) return;
        Position nextHead = snake.head().translate(queuedDirection);
        if (wrapWalls) nextHead = wrap(nextHead);
        boolean eating = food != null && food.position().equals(nextHead);
        directionQueuedThisTick = false;
        if ((!wrapWalls && outsideBoard(nextHead)) || snake.wouldHitSelf(nextHead, eating)) {
            status = GameStatus.GAME_OVER;
            return;
        }
        snake.moveTo(nextHead, queuedDirection, eating);
        if (eating) {
            score += food.type().points() * scoreMultiplier();
            foodEaten++;
            maximumLength = Math.max(maximumLength, snake.length());
            spawnFood();
        }
    }

    private boolean outsideBoard(Position position) {
        return position.x() < 0 || position.x() >= width
                || position.y() < 0 || position.y() >= height;
    }

    public void advanceTime(long elapsedMillis) {
        if (elapsedMillis < 0) throw new IllegalArgumentException("Elapsed time cannot be negative");
        if (elapsedMillis == 0 || status != GameStatus.RUNNING) return;
        playTimeMillis += elapsedMillis;
        if (food == null) return;
        foodRemainingMillis -= elapsedMillis;
        if (foodRemainingMillis < 1) spawnFood();
    }

    private void spawnFood() {
        FoodType type = random.nextInt(100) < BONUS_FOOD_PERCENT
                ? FoodType.BONUS : FoodType.NORMAL;
        food = FoodSpawner.spawn(width, height, snake, random, type).orElse(null);
        foodRemainingMillis = FOOD_LIFETIME_MILLIS;
        if (food == null) status = GameStatus.WON;
    }

    private Position wrap(Position position) {
        return new Position(Math.floorMod(position.x(), width), Math.floorMod(position.y(), height));
    }

    public int width() { return width; }
    public int height() { return height; }
    public Snake snake() { return snake; }
    public Food food() { return food; }
    public int score() { return score; }
    public GameStatus status() { return status; }
    public boolean wrapWalls() { return wrapWalls; }
    public long foodRemainingMillis() { return foodRemainingMillis; }

    public GameStatistics statistics() {
        double average = foodEaten == 0 ? 0 : (double) score / foodEaten;
        return new GameStatistics(foodEaten, maximumLength, playTimeMillis, average);
    }

    public int scoreMultiplier() {
        long freshnessBand = (Math.max(1, foodRemainingMillis) - 1) * 3 / FOOD_LIFETIME_MILLIS;
        return switch ((int) freshnessBand) {
            case 2 -> 3;
            case 1 -> 2;
            default -> 1;
        };
    }
}
