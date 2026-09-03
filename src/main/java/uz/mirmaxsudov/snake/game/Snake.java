package uz.mirmaxsudov.snake.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class Snake {
    private final Deque<Position> segments;
    private Direction direction;

    private Snake(List<Position> segments, Direction direction) {
        if (segments.isEmpty()) throw new IllegalArgumentException("A snake needs at least one segment");
        this.segments = new ArrayDeque<>(segments);
        this.direction = Objects.requireNonNull(direction);
    }

    public static Snake centered(int boardWidth, int boardHeight) {
        int headX = Math.max(3, boardWidth / 2);
        int y = boardHeight / 2;
        return new Snake(List.of(new Position(headX, y), new Position(headX - 1, y),
                new Position(headX - 2, y), new Position(headX - 3, y)), Direction.RIGHT);
    }

    public static Snake of(List<Position> segments, Direction direction) {
        return new Snake(List.copyOf(segments), direction);
    }

    public Position head() {
        return segments.getFirst();
    }

    public Direction direction() {
        return direction;
    }

    public int length() {
        return segments.size();
    }

    public List<Position> body() {
        return List.copyOf(segments);
    }

    public boolean occupies(Position position) {
        return segments.contains(position);
    }

    public boolean wouldHitSelf(Position nextHead, boolean growing) {
        Position tail = segments.getLast();
        return segments.contains(nextHead) && (growing || !tail.equals(nextHead));
    }

    public void move(Direction nextDirection, boolean grow) {
        moveTo(head().translate(nextDirection), nextDirection, grow);
    }

    void moveTo(Position nextHead, Direction nextDirection, boolean grow) {
        direction = Objects.requireNonNull(nextDirection);
        segments.addFirst(Objects.requireNonNull(nextHead));
        if (!grow) segments.removeLast();
    }
}
