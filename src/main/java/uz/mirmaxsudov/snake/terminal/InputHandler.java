package uz.mirmaxsudov.snake.terminal;

import org.jline.utils.NonBlockingReader;

import java.io.IOException;

public final class InputHandler {
    private final NonBlockingReader reader;

    public InputHandler(NonBlockingReader reader) {
        this.reader = reader;
    }

    public InputEvent poll(long timeoutMillis) throws IOException {
        int value = reader.read(Math.max(1, timeoutMillis));
        if (value < 0) return InputEvent.NONE;
        return decode(value);
    }

    private InputEvent decode(int value) throws IOException {
        return switch (value) {
            case 3, 'q', 'Q' -> InputEvent.QUIT;
            case '\r', '\n' -> InputEvent.ENTER;
            case 'w', 'W' -> InputEvent.UP;
            case 's', 'S' -> InputEvent.DOWN;
            case 'a', 'A' -> InputEvent.LEFT;
            case 'd', 'D' -> InputEvent.RIGHT;
            case 'p', 'P' -> InputEvent.PAUSE;
            case 'r', 'R' -> InputEvent.RESTART;
            case 'm', 'M' -> InputEvent.MENU;
            case 'y', 'Y' -> InputEvent.YES;
            case 'n', 'N' -> InputEvent.NO;
            case 27 -> decodeEscapeSequence();
            default -> InputEvent.NONE;
        };
    }

    private InputEvent decodeEscapeSequence() throws IOException {
        int first = reader.read(12);
        if (first != '[' && first != 'O') return InputEvent.BACK;
        return switch (reader.read(12)) {
            case 'A' -> InputEvent.UP;
            case 'B' -> InputEvent.DOWN;
            case 'C' -> InputEvent.RIGHT;
            case 'D' -> InputEvent.LEFT;
            default -> InputEvent.BACK;
        };
    }
}
