package uz.mirmaxsudov.snake.terminal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public final class Renderer {
    private final PrintWriter writer;
    private List<String> previousLines = List.of();
    private TerminalSize previousSize = new TerminalSize(0, 0);

    public Renderer(PrintWriter writer) {
        this.writer = writer;
    }

    public void render(List<String> content, TerminalSize size) {
        List<String> screen = center(content, size);

        if (!size.equals(previousSize)) {
            writer.print(Ansi.CLEAR_SCREEN + Ansi.HOME);
            previousLines = List.of();
            previousSize = size;
        }

        int lineCount = Math.max(previousLines.size(), screen.size());

        for (int index = 0; index < lineCount; index++) {
            String oldLine = index < previousLines.size() ? previousLines.get(index) : "";
            String newLine = index < screen.size() ? screen.get(index) : "";
            if (!newLine.equals(oldLine)) {
                writer.print(Ansi.cursor(index + 1, 1));
                writer.print(Ansi.CLEAR_LINE);
                writer.print(newLine);
            }
        }
        writer.flush();
        previousLines = screen;
    }

    public void invalidate() {
        previousLines = List.of();
        previousSize = new TerminalSize(0, 0);
    }

    private List<String> center(List<String> content, TerminalSize size) {
        int topPadding = Math.max(0, (size.rows() - content.size()) / 2);
        List<String> result = new ArrayList<>(topPadding + content.size());
        for (int i = 0; i < topPadding; i++) result.add("");
        for (String line : content) {
            int leftPadding = Math.max(0, (size.columns() - Ansi.visibleLength(line)) / 2);
            result.add(" ".repeat(leftPadding) + line);
        }
        return result;
    }
}
