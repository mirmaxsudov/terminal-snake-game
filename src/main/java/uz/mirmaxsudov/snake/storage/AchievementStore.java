package uz.mirmaxsudov.snake.storage;

import uz.mirmaxsudov.snake.game.Achievement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

public final class AchievementStore {
    private final Path file;

    public AchievementStore(Path file) { this.file = file; }

    public static AchievementStore defaultStore() {
        return new AchievementStore(Path.of(System.getProperty("user.home"), ".java-snake",
                "achievements.txt"));
    }

    public Set<Achievement> load() {
        EnumSet<Achievement> result = EnumSet.noneOf(Achievement.class);
        try {
            if (!Files.isRegularFile(file)) return result;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                try { result.add(Achievement.valueOf(line.strip())); }
                catch (IllegalArgumentException ignored) { }
            }
        } catch (IOException | SecurityException ignored) { }
        return result;
    }

    public void save(Set<Achievement> achievements) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, achievements.stream().map(Enum::name).sorted().toList(),
                    StandardCharsets.UTF_8);
        } catch (IOException | SecurityException ignored) { }
    }
}
