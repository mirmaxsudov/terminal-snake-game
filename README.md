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

In Two Players mode, Player 1 uses `WASD` and Player 2 uses the arrow keys.

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
- Optional, seed-deterministic obstacle fields that expand as levels advance
- Progressive levels every 150 points, with additional speed and obstacles
- Six food types: normal, bonus, speed, slow, shrink, and poison
- Shield, score boost, phase, and magnet power-ups with timed effects
- Three-second combo chains with multipliers up to x4
- Persistent local top-10 leaderboard with player, difficulty, level, and date
- Six persistent achievements with a dedicated progress screen
- Dedicated settings menu with editable player name and numeric game seed
- Deterministic seeded games for repeatable layouts and food sequences
- In-memory replay of the most recently completed game
- Enemy snakes that compete for food and turn every occupied segment into territory
- Three AI tiers: safe/random Simple, food-seeking Strategic, and BFS Pathfinder
- Local two-player arena mode with simultaneous movement and fair head-on collision rules
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

## Level 2 gameplay

Every 150 points advances the level. Higher levels accelerate movement and add
two new obstacles when obstacle mode is enabled. Food and power-up glyphs are:

| Glyph | Effect |
| --- | --- |
| `●` | Normal food |
| `★` | 30-point bonus food |
| `»` | Temporary speed increase |
| `◌` | Temporary slowdown |
| `▼` | Shrinks the snake by up to two segments |
| `×` | Removes 20 points and does not grow the snake |
| `S` | Shield that absorbs one collision |
| `2×` | Doubles positive food scores temporarily |
| `P` | Phases through walls and obstacles temporarily |
| `M` | Pulls nearby food toward the snake |

Eat again within three seconds to continue a combo. The combo score multiplier
increases at combo 3, 5, and 7, up to x4. Food freshness, combo, and power-up
multipliers stack.

## Arena modes

Choose `Vs AI` or `Two Players` under **Settings → Game Mode**. In an arena,
both snakes move on the same fixed tick and compete for the same normal and
bonus food. Hitting a wall, obstacle, your own body, or the opponent's body
loses the round. If both snakes crash on the same tick—including meeting
head-to-head or crossing heads—the result is a draw.

The AI difficulty setting provides three different controllers:

- **Simple** usually continues forward and otherwise picks a safe move.
- **Strategic** chooses safe moves that reduce its distance to food.
- **Pathfinder** uses breadth-first search for the shortest safe route and
  falls back to flood-fill open-space analysis when food is unreachable.

Player 1 uses `WASD`; Player 2 uses the arrow keys. In Solo and Vs AI modes,
either control scheme moves Player 1. Arena layouts remain repeatable when a
game seed is configured.

The Settings screen accepts a signed numeric seed. Reusing the same seed with
the same board and obstacle settings reproduces the same game. Leave the seed
empty for a new random game each time. The most recently completed game can be
played back from the main menu.

## High score

The high score is stored at:

```text
~/.java-snake/highscore.txt
```

Missing directories, malformed content, and write failures are handled
silently. The game remains playable when persistence is unavailable.

The leaderboard and achievements are stored alongside the high score as
`leaderboard.tsv` and `achievements.txt`.

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

The suite covers movement, growth, multi-food scoring, combos, levels,
power-ups, statistics, deterministic obstacles, enemy AI pathfinding,
simultaneous versus collisions, local multiplayer, board settings, snake styles,
accessible themes, persistence, wall collision and wrapping, confirmation UI,
food expiry, and safe spawning.
# terminal-snake-game
