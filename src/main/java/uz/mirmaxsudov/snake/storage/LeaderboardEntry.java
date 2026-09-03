package uz.mirmaxsudov.snake.storage;

import java.time.LocalDate;

public record LeaderboardEntry(String player, int score, String difficulty, int level,
                               LocalDate date) {
}
