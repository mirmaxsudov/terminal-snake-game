package uz.mirmaxsudov.snake.game;

import java.util.List;

public record ReplayData(
        long seed,
        int width,
        int height,
        boolean wrapWalls,
        boolean obstaclesEnabled,
        Difficulty difficulty,
        List<ReplayFrame> frames
) {
    public ReplayData {
        frames = List.copyOf(frames);
    }
}
