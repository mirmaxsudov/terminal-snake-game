# Java Snake

A polished, fully playable Snake game built for modern terminals with Java 21,
JLine, ANSI color, and Unicode. It runs entirely in the terminal: no Swing,
JavaFX, browser, or graphical window.

## Preview

```text
╭────────────────────────────────────────────────────────╮
│                       JAVA SNAKE                       │
├────────────────────────────────────────────────────────┤
│                                                        │
│                         ●                              │
│                                                        │
│                    ██████████                          │
│                                                        │
│                                                        │
├────────────────────────────────────────────────────────┤
│ SCORE 00040   HIGH 00250                   SPEED NORMAL │
│       WASD / ARROWS Move  ·  P Pause  ·  Q Quit        │
╰────────────────────────────────────────────────────────╯
```

The game uses the terminal's alternate screen, hides the cursor while active,
and redraws only rows whose contents changed to keep animation smooth.

## Requirements

- JDK 21 or newer
- Maven 3.9+
- An interactive terminal with ANSI and Unicode support
- A terminal window of at least 60 columns by 24 rows

Tested design targets include Windows Terminal, macOS Terminal, iTerm2, and
common Linux terminals. Classic Windows Command Prompt is not recommended.

## Build and run

```bash
mvn clean package
java -jar target/java-snake.jar
```

The Maven Shade plugin produces `target/java-snake.jar`, a self-contained
executable JAR including JLine and its Java 21-compatible native terminal
provider.

For development, package and launch with the same commands. Add
`-Dsnake.debug=true` before `-jar` to print a startup stack trace if terminal
initialization fails:

```bash
java -Dsnake.debug=true -jar target/java-snake.jar
```

## Controls

| Key | Action |
| --- | --- |
| `W` or `↑` | Move up |
| `S` or `↓` | Move down |
| `A` or `←` | Move left |
| `D` or `→` | Move right |
| `P` | Pause or resume |
| `R` | Restart after game over |
| `M` or `Esc` | Request to return to the main menu |
| `Q` or `Ctrl+C` | Request to quit |
| `Y`, `N`, or `Esc` | Answer a confirmation prompt |

The snake cannot reverse directly into itself. Only one turn is accepted per
movement tick, so very fast key sequences cannot bypass that rule.

## Features

- Fixed-timestep movement at Easy (150 ms), Normal (100 ms), or Hard (65 ms)
- Gradual, bounded speed increase as the score grows
- Default, Matrix, Ocean, Monochrome, High Contrast, and Color Safe themes
- Small (20 × 12), Medium (28 × 16), and Large (40 × 20) board sizes
- Blocks, Circles, ASCII, and Diamonds snake styles
- Reliable food placement on unoccupied cells
- Wall and self-collision detection
- Pause and game-over overlays
- 3-2-1-GO countdown before every game and restart
- Freshness multipliers: collect food quickly for x3 or x2 points
- Nine-second food expiration with automatic safe respawning
- 15% chance of bonus star food worth 30 base points
- Optional wall-wrap mode, configurable from the main menu
- End-of-game statistics for food eaten, maximum length, active play time, and average points
- Confirmation screens before quitting or abandoning a game
- Locally persisted high score
- Selectable board sizes up to 40 × 20 logical cells
- Live terminal-resize detection and automatic recovery
- Low-output, line-diff terminal rendering
- Defensive raw-mode, cursor, ANSI, wrapping, and alternate-screen cleanup

Each logical game cell uses two terminal columns so the board remains visually
proportional. Larger board choices may ask you to enlarge the terminal before
play starts; pressing `M` or `Esc` returns safely to the menu.

## Architecture

```text
uz.mirmaxsudov
├── Main.java                         application entry point
└── snake
    ├── SnakeApplication.java         menus and fixed-timestep state machine
    ├── game/                         deterministic game model
    ├── terminal/                     JLine lifecycle, input, ANSI, renderer
    ├── ui/                           themes and screen composition
    └── storage/HighScoreStore.java   fault-tolerant local persistence
```

The game model is independent of terminal I/O and covered by unit tests.
JLine's non-blocking reader is polled on the game thread, avoiding a background
input thread while keeping movement responsive.

## High score

The high score is stored at:

```text
~/.java-snake/highscore.txt
```

Missing directories, malformed content, and write failures are handled
silently. The game remains playable when persistence is unavailable.

## Troubleshooting

- **Terminal too small:** resize to at least 60 × 24. The game pauses while the
  warning is visible and resumes after the terminal is large enough.
- **Garbled borders or blocks:** choose a terminal font with Unicode box-drawing
  and block-glyph support.
- **No color:** ensure ANSI/VT processing is enabled. Use Windows Terminal
  instead of legacy Command Prompt on Windows.
- **Java Snake needs an interactive terminal:** run the JAR from Windows
  Terminal, iTerm2, macOS Terminal, or a Linux terminal—not from an IDE
  Run/Debug output console and not with redirected input/output. The packaged
  application explicitly selects JLine's cross-platform JNI provider.
- **Terminal looks altered after an abnormal stop:** run `reset` on macOS/Linux
  or reopen the terminal tab. Normal exits, errors, `Q`, and raw `Ctrl+C`
  restore terminal state automatically.

## Tests

```bash
mvn test
```

The suite covers movement, growth, scoring, statistics, board settings, snake
styles, accessible theme cycling, wall collision and wrapping, direction
validation, confirmation UI, food expiry, bonus food, and safe food spawning.
# terminal-snake-game
