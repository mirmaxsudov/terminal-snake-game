package uz.mirmaxsudov.snake.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardSizeTest {
    @Test
    void cyclesThroughAllConfiguredSizes() {
        assertEquals(BoardSize.MEDIUM, BoardSize.SMALL.next());
        assertEquals(BoardSize.LARGE, BoardSize.MEDIUM.next());
        assertEquals(BoardSize.SMALL, BoardSize.LARGE.next());
    }

    @Test
    void exposesPlayableDimensions() {
        assertEquals(20, BoardSize.SMALL.width());
        assertEquals(12, BoardSize.SMALL.height());
        assertEquals(40, BoardSize.LARGE.width());
        assertEquals(20, BoardSize.LARGE.height());
    }
}
