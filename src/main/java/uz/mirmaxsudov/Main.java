package uz.mirmaxsudov;

import uz.mirmaxsudov.snake.SnakeApplication;
import uz.mirmaxsudov.snake.storage.HighScoreStore;
import uz.mirmaxsudov.snake.terminal.TerminalManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try (TerminalManager terminal = TerminalManager.open()) {
            new SnakeApplication(terminal, HighScoreStore.defaultStore()).run();
        } catch (Exception exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains("Unable to create a terminal")) {
                System.err.println("Java Snake needs an interactive terminal.");
                System.err.println("Run the JAR from Windows Terminal, iTerm2, macOS Terminal,");
                System.err.println("or a Linux terminal—not an IDE Run/Debug output console.");
            } else {
                System.err.println("Java Snake could not start: " + exception.getMessage());
            }
            if (Boolean.getBoolean("snake.debug")) exception.printStackTrace(System.err);
        }
    }
}
