package uz.mirmaxsudov.snake.terminal;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TerminalManager implements AutoCloseable {
    private final Terminal terminal;
    private final Attributes originalAttributes;
    private final PrintWriter writer;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Thread cleanupHook;

    private TerminalManager(Terminal terminal) {
        this.terminal = terminal;
        this.originalAttributes = terminal.enterRawMode();
        this.writer = terminal.writer();
        this.cleanupHook = new Thread(this::restore, "java-snake-terminal-cleanup");
        try {
            Runtime.getRuntime().addShutdownHook(cleanupHook);
            writer.print(Ansi.ALT_SCREEN + Ansi.CLEAR_SCREEN + Ansi.HOME
                    + Ansi.HIDE_CURSOR + Ansi.DISABLE_WRAP);
            writer.flush();
        } catch (RuntimeException | Error failure) {
            terminal.setAttributes(originalAttributes);
            try {
                terminal.close();
            } catch (IOException e) {
                failure.addSuppressed(e);
            }
            throw failure;
        }
    }

    public static TerminalManager open() throws IOException {
        return new TerminalManager(TerminalBuilder
                .builder()
                .system(true)
                .provider("jni")
                .dumb(false)
                .build());
    }

    public NonBlockingReader reader() {
        return terminal.reader();
    }

    public PrintWriter writer() {
        return writer;
    }

    public TerminalSize size() {
        return new TerminalSize(Math.max(1, terminal.getWidth()), Math.max(1, terminal.getHeight()));
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        restore();
        try {
            Runtime.getRuntime().removeShutdownHook(cleanupHook);
        } catch (IllegalStateException ignored) {
            // JVM shutdown is already in progress.
        }
        terminal.setAttributes(originalAttributes);
        try {
            terminal.close();
        } catch (IOException ignored) {
            // Display and terminal attributes have already been restored.
        }
    }

    private synchronized void restore() {
        writer.print(Ansi.RESET + Ansi.ENABLE_WRAP + Ansi.SHOW_CURSOR + Ansi.MAIN_SCREEN);
        writer.flush();
        if (!closed.get()) terminal.setAttributes(originalAttributes);
    }
}
