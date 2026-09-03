package uz.mirmaxsudov.snake.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class AiController {
    private static final List<Direction> DIRECTIONS = List.of(
            Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);

    private AiController() {
    }

    public static Direction choose(AiDifficulty difficulty, Snake snake, Snake opponent,
                                   Position food, Set<Position> obstacles, int width, int height,
                                   boolean wrapWalls, RandomGenerator random) {
        Objects.requireNonNull(difficulty);
        Objects.requireNonNull(snake);
        Objects.requireNonNull(opponent);
        Objects.requireNonNull(obstacles);
        Objects.requireNonNull(random);

        List<Direction> legal = legalDirections(snake, opponent, obstacles, width, height, wrapWalls);

        if (legal.isEmpty()) return snake.direction();
        return switch (difficulty) {
            case SIMPLE -> casual(legal, snake.direction(), random);
            case STRATEGIC -> strategic(legal, snake, opponent, food, obstacles,
                    width, height, wrapWalls);
            case PATHFINDER -> pathfinder(legal, snake, opponent, food, obstacles,
                    width, height, wrapWalls);
        };
    }

    private static Direction casual(List<Direction> legal, Direction current,
                                    RandomGenerator random) {
        if (legal.contains(current) && random.nextInt(100) < 65) return current;
        return legal.get(random.nextInt(legal.size()));
    }

    private static Direction strategic(List<Direction> legal, Snake snake, Snake opponent,
                                       Position food, Set<Position> obstacles, int width,
                                       int height, boolean wrapWalls) {
        return legal.stream().min((left, right) -> Integer.compare(
                strategicCost(next(snake.head(), left, width, height, wrapWalls), food,
                        opponent, obstacles, width, height, wrapWalls),
                strategicCost(next(snake.head(), right, width, height, wrapWalls), food,
                        opponent, obstacles, width, height, wrapWalls))).orElse(snake.direction());
    }

    private static int strategicCost(Position position, Position food, Snake opponent,
                                     Set<Position> obstacles, int width, int height,
                                     boolean wrapWalls) {
        int cost = food == null ? 0 : distance(position, food, width, height, wrapWalls) * 10;
        Position opponentNext = next(opponent.head(), opponent.direction(), width, height, wrapWalls);
        if (position.equals(opponentNext)) cost += 1_000;
        int exits = 0;
        for (Direction direction : DIRECTIONS) {
            Position candidate = next(position, direction, width, height, wrapWalls);
            if (candidate != null && !obstacles.contains(candidate) && !opponent.occupies(candidate)) exits++;
        }
        return cost - exits;
    }

    private static Direction pathfinder(List<Direction> legal, Snake snake, Snake opponent,
                                        Position food, Set<Position> obstacles, int width,
                                        int height, boolean wrapWalls) {
        Set<Position> blocked = new HashSet<>(obstacles);
        blocked.addAll(snake.body());
        blocked.addAll(opponent.body());
        blocked.remove(snake.head());
        blocked.remove(snake.body().getLast());
        blocked.remove(opponent.body().getLast());

        if (food != null) {
            Direction shortest = shortestFirstStep(snake.head(), food, legal, blocked,
                    width, height, wrapWalls);
            if (shortest != null) return shortest;
        }

        Direction best = legal.getFirst();
        int bestSpace = -1;
        for (Direction direction : legal) {
            Position start = next(snake.head(), direction, width, height, wrapWalls);
            int space = reachableSpace(start, blocked, width, height, wrapWalls);
            if (space > bestSpace) {
                best = direction;
                bestSpace = space;
            }
        }
        return best;
    }

    private static Direction shortestFirstStep(Position start, Position target,
                                               List<Direction> legal, Set<Position> blocked,
                                               int width, int height, boolean wrapWalls) {
        Queue<Position> queue = new ArrayDeque<>();
        Map<Position, Direction> firstSteps = new HashMap<>();
        Set<Position> visited = new HashSet<>();
        visited.add(start);
        for (Direction direction : legal) {
            Position candidate = next(start, direction, width, height, wrapWalls);
            if (candidate == null || blocked.contains(candidate)) continue;
            if (candidate.equals(target)) return direction;
            if (visited.add(candidate)) {
                queue.add(candidate);
                firstSteps.put(candidate, direction);
            }
        }
        while (!queue.isEmpty()) {
            Position current = queue.remove();
            Direction first = firstSteps.get(current);
            for (Direction direction : DIRECTIONS) {
                Position candidate = next(current, direction, width, height, wrapWalls);
                if (candidate == null || blocked.contains(candidate) || !visited.add(candidate)) continue;
                if (candidate.equals(target)) return first;
                queue.add(candidate);
                firstSteps.put(candidate, first);
            }
        }
        return null;
    }

    private static int reachableSpace(Position start, Set<Position> blocked, int width,
                                      int height, boolean wrapWalls) {
        if (start == null || blocked.contains(start)) return 0;
        Queue<Position> queue = new ArrayDeque<>();
        Set<Position> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Position current = queue.remove();
            for (Direction direction : DIRECTIONS) {
                Position candidate = next(current, direction, width, height, wrapWalls);
                if (candidate != null && !blocked.contains(candidate) && visited.add(candidate)) {
                    queue.add(candidate);
                }
            }
        }
        return visited.size();
    }

    private static List<Direction> legalDirections(Snake snake, Snake opponent,
                                                   Set<Position> obstacles, int width,
                                                   int height, boolean wrapWalls) {
        List<Direction> result = new ArrayList<>();
        for (Direction direction : DIRECTIONS) {
            if (direction.isOpposite(snake.direction())) continue;
            Position candidate = next(snake.head(), direction, width, height, wrapWalls);
            if (candidate == null || obstacles.contains(candidate)) continue;
            boolean ownCollision = snake.occupies(candidate)
                    && !candidate.equals(snake.body().getLast());
            boolean enemyCollision = opponent.occupies(candidate)
                    && !candidate.equals(opponent.body().getLast());
            if (!ownCollision && !enemyCollision) result.add(direction);
        }
        return result;
    }

    private static Position next(Position position, Direction direction, int width, int height,
                                 boolean wrapWalls) {
        Position candidate = position.translate(direction);
        if (wrapWalls) {
            return new Position(Math.floorMod(candidate.x(), width), Math.floorMod(candidate.y(), height));
        }
        if (candidate.x() < 0 || candidate.x() >= width || candidate.y() < 0 || candidate.y() >= height) {
            return null;
        }
        return candidate;
    }

    private static int distance(Position first, Position second, int width, int height,
                                boolean wrapWalls) {
        int dx = Math.abs(first.x() - second.x());
        int dy = Math.abs(first.y() - second.y());
        if (wrapWalls) {
            dx = Math.min(dx, width - dx);
            dy = Math.min(dy, height - dy);
        }
        return dx + dy;
    }
}
