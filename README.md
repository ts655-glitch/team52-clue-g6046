# Clue! - Murder Mystery Game

A Java implementation of the classic board game Clue! (Cluedo), built by Team 52.

The main version of the game is a JavaFX GUI with a visual board, player setup screen,
detective notebook, human turns, AI turns, suggestions, disprovals and accusations.
A console entry point is also included for the earlier text-based version.

## Requirements

- Java 21+
- JavaFX SDK 21+ for the GUI

The project was developed with JavaFX SDK 21.0.11.

## How to Run the GUI

From the project root, compile the source files and copy the board image into the
output directory:

```bash
mkdir -p out
javac --module-path /Applications/javafx-sdk-21.0.11/lib --add-modules javafx.controls,javafx.graphics -d out $(find src -name "*.java")
cp src/resources/board.png out/board.png
```

Then launch the JavaFX application:

```bash
java --module-path /Applications/javafx-sdk-21.0.11/lib --add-modules javafx.controls,javafx.graphics -cp out com.cluegame.gui.ClueGameApp
```

If your JavaFX SDK is installed somewhere else, replace
`/Applications/javafx-sdk-21.0.11/lib` with the path to your local JavaFX `lib`
folder.

## Console Version

The console version can be launched from the same compiled output:

```bash
java -cp out com.cluegame.Main
```

The GUI is the recommended version for normal play.

## Game Setup

- Choose the number of human and AI players from the setup screen.
- The game supports 2-6 total players.
- Human players choose their character names and tokens.
- AI players automatically take the remaining available characters.
- The deck is shuffled, then one suspect, one weapon and one room are sealed in the murder envelope.
- The remaining cards are dealt to the players.

## Game Overview

A murder has been committed. One suspect, one weapon and one room are hidden in
the murder envelope. Players must deduce the solution by moving around the board,
making suggestions, seeing disproval cards and eliminating possibilities.

### Suspects

Miss Scarlett, Colonel Mustard, Mrs White, Reverend Green, Mrs Peacock, Professor Plum

### Weapons

Candlestick, Dagger, Lead Piping, Revolver, Rope, Spanner

### Rooms

Kitchen, Ballroom, Conservatory, Billiard Room, Library, Study, Hall, Lounge, Dining Room

## Turn Structure

Each active player takes a turn in order.

### Movement

- If a player is in a corridor, they roll the dice and move by clicking highlighted valid squares.
- If a player reaches a room, movement ends immediately.
- If a player starts in a room, they can stay, leave through a door, or use a secret passage where one exists.
- Secret passages connect Kitchen with Study and Lounge with Conservatory.

### Suggestions

- Suggestions can be made from inside a room.
- The room in the suggestion is always the player's current room.
- The suggested suspect token is moved into that room.
- Other players are checked in turn order to see whether they can disprove the suggestion.
- If a human player has multiple matching cards, they privately choose which card to show.
- AI players choose a matching card automatically.
- Eliminated players can still disprove suggestions because they still hold their cards.

### Accusations

- A player may make an accusation when they think they know the suspect, weapon and room.
- A correct accusation wins the game immediately.
- An incorrect accusation eliminates that player from winning, but they remain available to disprove suggestions.
- If all human players are eliminated, the AI players continue resolving the game automatically.

## Detective Notebook

The GUI includes a detective notebook for tracking suspects, weapons and rooms.
Each human player has their own notebook state, so notes are kept separate during
multi-human games.

## AI Players

AI players move, suggest, disprove and accuse automatically. They use information
from their own hand, seen disproval cards and undisproved suggestions to narrow
down the murder envelope. AI players only make a final accusation when they have
enough evidence to identify the suspect, weapon and room.

## Multiple Human Players

Multiple human players can play on the same screen. The GUI uses handoff prompts
between human turns and private card-choice prompts when a human player needs to
disprove another player's suggestion. This helps keep each player's hand and
notebook private during local play.

## Testing

The project includes JUnit tests for the card model, board logic, players, GUI
controller flow, suggestions and accusations.

If JUnit is available locally, compile and run the tests from the project root.
The latest verified test run passed all tests.
