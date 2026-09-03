package uz.mirmaxsudov.snake.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class Game {
    public static final int POINTS_PER_FOOD = 10;
    public static final long FOOD_LIFETIME_MILLIS = 9_000;
    public static final long POWER_UP_LIFETIME_MILLIS = 7_000;
    public static final long COMBO_WINDOW_MILLIS = 3_000;
    public static final int LEVEL_SCORE_STEP = 150;
    private static final long FOOD_EFFECT_MILLIS = 5_000;

    private final int width;
    private final int height;
    private final RandomGenerator random;
    private final Snake snake;
    private final boolean wrapWalls;
    private final boolean obstaclesEnabled;
    private final Set<Position> obstacles = new LinkedHashSet<>();
    private final Map<PowerUpType, Long> effects = new EnumMap<>(PowerUpType.class);
    private Food food;
    private PowerUp powerUp;
    private long foodRemainingMillis = FOOD_LIFETIME_MILLIS;
    private long powerUpRemainingMillis;
    private long comboRemainingMillis;
    private long speedEffectMillis;
    private long slowEffectMillis;
    private Direction queuedDirection;
    private boolean directionQueuedThisTick;
    private int score;
    private int foodEaten;
    private int maximumLength;
    private int combo;
    private int maximumCombo;
    private int level = 1;
    private long playTimeMillis;
    private GameStatus status = GameStatus.RUNNING;

    public Game(int width, int height, RandomGenerator random) {
        this(width, height, random, false, false);
    }

    public Game(int width, int height, RandomGenerator random, boolean wrapWalls) {
        this(width, height, random, wrapWalls, false);
    }

    public Game(int width, int height, RandomGenerator random, boolean wrapWalls,
                boolean obstaclesEnabled) {
        this(width, height, Snake.centered(width, height), null, random, wrapWalls,
                obstaclesEnabled);
        if (obstaclesEnabled) addObstacles(Math.max(3, width * height / 100));
        spawnFood();
    }

    Game(int width, int height, Snake snake, Food food, RandomGenerator random) {
        this(width, height, snake, food, random, false, false);
    }

    Game(int width, int height, Snake snake, Food food, RandomGenerator random,
         boolean wrapWalls) {
        this(width, height, snake, food, random, wrapWalls, false);
    }

    Game(int width, int height, Snake snake, Food food, RandomGenerator random,
         boolean wrapWalls, boolean obstaclesEnabled) {
        if (width < 8 || height < 6) throw new IllegalArgumentException("Board must be at least 8x6");
        this.width = width;
        this.height = height;
        this.snake = Objects.requireNonNull(snake);
        this.food = food;
        this.random = Objects.requireNonNull(random);
        this.wrapWalls = wrapWalls;
        this.obstaclesEnabled = obstaclesEnabled;
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
        boolean phase = hasEffect(PowerUpType.PHASE);
        Position nextHead = snake.head().translate(queuedDirection);
        if (wrapWalls || phase) nextHead = wrap(nextHead);
        boolean eating = food != null && food.position().equals(nextHead);
        boolean growing = eating && food.type().growsSnake();
        directionQueuedThisTick = false;

        boolean collision = (!wrapWalls && !phase && outsideBoard(nextHead))
                || (!phase && obstacles.contains(nextHead))
                || snake.wouldHitSelf(nextHead, growing);
        if (collision) {
            if (hasEffect(PowerUpType.SHIELD)) {
                effects.remove(PowerUpType.SHIELD);
                return;
            }
            status = GameStatus.GAME_OVER;
            return;
        }

        boolean collectingPowerUp = powerUp != null && powerUp.position().equals(nextHead);
        snake.moveTo(nextHead, queuedDirection, growing);
        if (eating) consumeFood();
        if (collectingPowerUp) activatePowerUp();
        pullFoodWithMagnet();
    }

    private void consumeFood() {
        FoodType type = food.type();
        combo = comboRemainingMillis > 0 ? combo + 1 : 1;
        maximumCombo = Math.max(maximumCombo, combo);
        comboRemainingMillis = COMBO_WINDOW_MILLIS;

        int basePoints = type.points();
        if (basePoints < 0) {
            score = Math.max(0, score + basePoints);
        } else {
            int boost = hasEffect(PowerUpType.SCORE_BOOST) ? 2 : 1;
            score += basePoints * scoreMultiplier() * comboMultiplier() * boost;
        }
        foodEaten++;

        switch (type) {
            case SPEED -> speedEffectMillis = FOOD_EFFECT_MILLIS;
            case SLOW -> slowEffectMillis = FOOD_EFFECT_MILLIS;
            case SHRINK -> snake.shrink(2);
            case NORMAL, BONUS, POISON -> { }
        }
        maximumLength = Math.max(maximumLength, snake.length());
        updateLevel();
        maybeSpawnPowerUp();
        spawnFood();
    }

    private void updateLevel() {
        int targetLevel = score / LEVEL_SCORE_STEP + 1;
        while (level < targetLevel) {
            level++;
            if (obstaclesEnabled) addObstacles(2);
        }
    }

    private void maybeSpawnPowerUp() {
        if (powerUp != null || random.nextInt(100) >= 20) return;
        PowerUpType[] types = PowerUpType.values();
        freePosition().ifPresent(position -> {
            powerUp = new PowerUp(position, types[random.nextInt(types.length)]);
            powerUpRemainingMillis = POWER_UP_LIFETIME_MILLIS;
        });
    }

    private void activatePowerUp() {
        effects.put(powerUp.type(), powerUp.type().durationMillis());
        powerUp = null;
        powerUpRemainingMillis = 0;
    }

    public void advanceTime(long elapsedMillis) {
        if (elapsedMillis < 0) throw new IllegalArgumentException("Elapsed time cannot be negative");
        if (elapsedMillis == 0 || status != GameStatus.RUNNING) return;
        playTimeMillis += elapsedMillis;
        speedEffectMillis = Math.max(0, speedEffectMillis - elapsedMillis);
        slowEffectMillis = Math.max(0, slowEffectMillis - elapsedMillis);

        if (comboRemainingMillis > 0) {
            comboRemainingMillis = Math.max(0, comboRemainingMillis - elapsedMillis);
            if (comboRemainingMillis == 0) combo = 0;
        }
        effects.replaceAll((type, remaining) -> Math.max(0, remaining - elapsedMillis));
        effects.values().removeIf(remaining -> remaining == 0);

        if (powerUp != null) {
            powerUpRemainingMillis -= elapsedMillis;
            if (powerUpRemainingMillis <= 0) {
                powerUp = null;
                powerUpRemainingMillis = 0;
            }
        }
        if (food != null) {
            foodRemainingMillis -= elapsedMillis;
            if (foodRemainingMillis <= 0) spawnFood();
        }
    }

    private void spawnFood() {
        Set<Position> blocked = new LinkedHashSet<>(obstacles);
        if (powerUp != null) blocked.add(powerUp.position());
        food = FoodSpawner.spawn(width, height, snake, random, randomFoodType(), blocked).orElse(null);
        foodRemainingMillis = FOOD_LIFETIME_MILLIS;
        if (food == null) status = GameStatus.WON;
    }

    private FoodType randomFoodType() {
        int roll = random.nextInt(100);
        if (roll < 55) return FoodType.NORMAL;
        if (roll < 70) return FoodType.BONUS;
        if (roll < 80) return FoodType.SPEED;
        if (roll < 90) return FoodType.SLOW;
        if (roll < 95) return FoodType.SHRINK;
        return FoodType.POISON;
    }

    private void addObstacles(int count) {
        for (int i = 0; i < count; i++) freePosition().ifPresent(obstacles::add);
    }

    private Optional<Position> freePosition() {
        List<Position> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position candidate = new Position(x, y);
                if (!snake.occupies(candidate) && !obstacles.contains(candidate)
                        && (food == null || !food.position().equals(candidate))
                        && (powerUp == null || !powerUp.position().equals(candidate))) {
                    free.add(candidate);
                }
            }
        }
        return free.isEmpty() ? Optional.empty()
                : Optional.of(free.get(random.nextInt(free.size())));
    }

    private void pullFoodWithMagnet() {
        if (!hasEffect(PowerUpType.MAGNET) || food == null) return;
        Position head = snake.head();
        Position current = food.position();
        int distance = Math.abs(head.x() - current.x()) + Math.abs(head.y() - current.y());
        if (distance <= 1 || distance > 5) return;
        int dx = Integer.compare(head.x(), current.x());
        int dy = Integer.compare(head.y(), current.y());
        Position candidate = Math.abs(head.x() - current.x()) >= Math.abs(head.y() - current.y())
                ? new Position(current.x() + dx, current.y())
                : new Position(current.x(), current.y() + dy);
        if (!snake.occupies(candidate) && !obstacles.contains(candidate)
                && (powerUp == null || !powerUp.position().equals(candidate))) {
            food = new Food(candidate, food.type());
        }
    }

    private boolean outsideBoard(Position position) {
        return position.x() < 0 || position.x() >= width
                || position.y() < 0 || position.y() >= height;
    }

    private Position wrap(Position position) {
        return new Position(Math.floorMod(position.x(), width), Math.floorMod(position.y(), height));
    }

    public int scoreMultiplier() {
        long band = (Math.max(1, foodRemainingMillis) - 1) * 3 / FOOD_LIFETIME_MILLIS;
        return band == 2 ? 3 : band == 1 ? 2 : 1;
    }

    public int comboMultiplier() { return Math.min(4, 1 + Math.max(0, combo - 1) / 2); }
    public boolean hasEffect(PowerUpType type) { return effects.getOrDefault(type, 0L) > 0; }
    public long effectRemainingMillis(PowerUpType type) { return effects.getOrDefault(type, 0L); }
    public double speedFactor() {
        double factor = 1.0;
        if (speedEffectMillis > 0) factor *= 0.75;
        if (slowEffectMillis > 0) factor *= 1.35;
        return factor;
    }

    public GameStatistics statistics() {
        double average = foodEaten == 0 ? 0 : (double) score / foodEaten;
        return new GameStatistics(foodEaten, maximumLength, playTimeMillis, average, level,
                maximumCombo);
    }

    public int width() { return width; }
    public int height() { return height; }
    public Snake snake() { return snake; }
    public Food food() { return food; }
    public PowerUp powerUp() { return powerUp; }
    public Set<Position> obstacles() { return Set.copyOf(obstacles); }
    public int score() { return score; }
    public int level() { return level; }
    public int combo() { return combo; }
    public Direction queuedDirection() { return queuedDirection; }
    public GameStatus status() { return status; }
    public boolean wrapWalls() { return wrapWalls; }
    public boolean obstaclesEnabled() { return obstaclesEnabled; }
    public long foodRemainingMillis() { return foodRemainingMillis; }
    public long powerUpRemainingMillis() { return powerUpRemainingMillis; }
}
