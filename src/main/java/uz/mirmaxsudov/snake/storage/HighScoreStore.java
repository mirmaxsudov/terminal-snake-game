package uz.mirmaxsudov.snake.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HighScoreStore {
    private final Path file;

    public HighScoreStore(Path file) {
        this.file = file;
    }

    public static HighScoreStore defaultStore() {
        return new HighScoreStore(Path.of(System.getProperty("user.home"), ".java-snake", "highscore.txt"));
    }

    public int load() {
        try {
            if (!Files.isRegularFile(file)) return 0;
            int score = Integer.parseInt(Files.readString(file, StandardCharsets.UTF_8).trim());
            return Math.max(0, score);
        } catch (IOException | NumberFormatException | SecurityException ignored) {
            return 0;
        }
    }

    public void save(int score) {
        if (score < 0) return;
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(file, Integer.toString(score), StandardCharsets.UTF_8);
        } catch (IOException | SecurityException ignored) {
            // Persistence is optional; gameplay continues when the file is not writable.
        }
    }
}
