package uz.mirmaxsudov.snake.storage;

import org.junit.jupiter.api.Test;
import uz.mirmaxsudov.snake.game.Achievement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressStoreTest {
    @Test
    void leaderboardKeepsTheTenHighestScores() throws IOException {
        Path directory = Files.createTempDirectory(Path.of("target"), "leaderboard-test-");
        LeaderboardStore store = new LeaderboardStore(directory.resolve("leaderboard.tsv"));
        for (int score = 0; score < 12; score++) {
            store.add(new LeaderboardEntry("P" + score, score, "Normal", 1,
                    LocalDate.of(2026, 1, 1)));
        }

        assertEquals(10, store.load().size());
        assertEquals(11, store.load().getFirst().score());
        assertEquals(2, store.load().getLast().score());
    }

    @Test
    void achievementsRoundTrip() throws IOException {
        Path directory = Files.createTempDirectory(Path.of("target"), "achievement-test-");
        AchievementStore store = new AchievementStore(directory.resolve("achievements.txt"));
        store.save(EnumSet.of(Achievement.FIRST_BITE, Achievement.CENTURY));

        assertEquals(EnumSet.of(Achievement.FIRST_BITE, Achievement.CENTURY), store.load());
    }
}
