# Clue! — Murder Mystery Game (Console Prototype)

A Java console-based implementation of the classic board game Clue! (Cluedo). Built by Team 52.

## How to Run

Requires **Java 21+**.

```bash
javac -d out src/com/cluegame/**/*.java src/com/cluegame/*.java
java -cp out com.cluegame.Main
```

On launch you'll be asked how many **human** and **AI** players to include (2-6 total, at least 1 AI).

## Game Overview

A murder has been committed. One **suspect**, one **weapon** and one **room** are sealed in the murder envelope. Your goal is to figure out all three by making suggestions and eliminating possibilities.

### Suspects
Miss Scarlett, Colonel Mustard, Mrs White, Reverend Green, Mrs Peacock, Professor Plum

### Weapons
Candlestick, Dagger, Lead Piping, Revolver, Rope, Spanner

### Rooms
Kitchen, Ballroom, Conservatory, Billiard Room, Library, Study, Hall, Lounge, Dining Room

## Setup

- The deck is shuffled and one suspect, weapon and room card are placed in the murder envelope (hidden).
- The remaining 18 cards are dealt evenly to all players.
- Cards in your hand **cannot** be in the envelope — use this to eliminate possibilities.
- Each player is placed on their starting square on the board.

## Turn Structure

Each turn has up to three phases:

### 1. Movement

**If you're in a room:**
- If a **secret passage** exists (e.g. Kitchen <-> Study, Lounge <-> Conservatory), you can type `Y` to use it. This ends your movement.
- Otherwise, pick a **numbered door** to exit through, or choose `0` to stay.

**If you're in a corridor:**
- Roll the dice automatically.
- Move step by step using direction commands:
  - `N` = North (up), `S` = South (down), `E` = East (right), `W` = West (left)
- The game shows which directions are valid each step.
- Type `R` to enter a room if you're standing on its door.
- Type `P` to stop moving early and pass remaining steps.
- Entering a room **ends your movement immediately**.

### 2. Suggestion (if in a room)

- You can suggest a suspect and weapon — the room is automatically your current room.
- The accused suspect's piece gets moved into the room.
- Going clockwise, each other player checks their hand:
  - The **first** player with a matching card must privately show you one.
  - If they have multiple matches, they choose which to show.
  - If no one can disprove, the suggestion stands.
- Eliminated players still disprove suggestions (they keep their cards).

### 3. Accusation (optional, only after a suggestion)

- You can formally accuse a suspect, weapon **and** room (any room, not just your current one).
- If **correct** — you win the game!
- If **wrong** — you are **eliminated**. You stay in the game only to disprove other players' suggestions.

## How to Win

Deduce which suspect, weapon and room are in the murder envelope by process of elimination:
1. Cards in your hand are **not** in the envelope.
2. Cards shown to you by other players are **not** in the envelope.
3. When you've eliminated all but one option in each category, make your accusation.

## AI Players

AI players move, suggest and accuse automatically. They:
- Navigate toward rooms they haven't visited yet.
- Suggest suspects/weapons they haven't seen, to gather new information.
- Only accuse when they've narrowed each category down to exactly one possibility.

## Multiple Human Players

When multiple humans are playing on the same screen, the game shows a **handoff screen** between turns so the previous player's information stays private. Press Enter when prompted to continue.