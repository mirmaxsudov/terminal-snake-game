package uz.mirmaxsudov.snake.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LeaderboardStore {
    private static final int LIMIT = 10;
    private final Path file;

    public LeaderboardStore(Path file) { this.file = file; }

    public static LeaderboardStore defaultStore() {
        return new LeaderboardStore(Path.of(System.getProperty("user.home"), ".java-snake",
                "leaderboard.tsv"));
    }

    public List<LeaderboardEntry> load() {
        try {
            if (!Files.isRegularFile(file)) return List.of();
            List<LeaderboardEntry> entries = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] values = line.split("\\t", -1);
                if (values.length != 5) continue;
                try {
                    entries.add(new LeaderboardEntry(values[0], Integer.parseInt(values[1]),
                            values[2], Integer.parseInt(values[3]), LocalDate.parse(values[4])));
                } catch (RuntimeException ignored) { }
            }
            return ranked(entries);
        } catch (IOException | SecurityException ignored) {
            return List.of();
        }
    }

    public void add(LeaderboardEntry entry) {
        List<LeaderboardEntry> entries = new ArrayList<>(load());
        entries.add(entry);
        entries = ranked(entries);
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            List<String> lines = entries.stream().map(value -> String.join("\t",
                    sanitize(value.player()), Integer.toString(value.score()),
                    sanitize(value.difficulty()), Integer.toString(value.level()),
                    value.date().toString())).toList();
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException | SecurityException ignored) { }
    }

    private List<LeaderboardEntry> ranked(List<LeaderboardEntry> entries) {
        return entries.stream()
                .sorted(Comparator.comparingInt(LeaderboardEntry::score).reversed()
                        .thenComparing(LeaderboardEntry::date))
                .limit(LIMIT).toList();
    }

    private String sanitize(String value) {
        return value.replace('\t', ' ').replace('\n', ' ').strip();
    }
}
