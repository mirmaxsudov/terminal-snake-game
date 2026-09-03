package uz.mirmaxsudov.snake.game;

public enum MatchStatus {
    RUNNING,
    PAUSED,
    PLAYER_ONE_WON,
    PLAYER_TWO_WON,
    DRAW;

    public boolean finished() {
        return this == PLAYER_ONE_WON || this == PLAYER_TWO_WON || this == DRAW;
    }
}
