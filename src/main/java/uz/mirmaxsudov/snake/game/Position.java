package uz.mirmaxsudov.snake.game;

public record Position(int x, int y) {
    public Position translate(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }
}
