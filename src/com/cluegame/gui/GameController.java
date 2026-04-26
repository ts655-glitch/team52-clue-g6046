package com.cluegame.gui;

import com.cluegame.cards.Card;
import com.cluegame.cards.MurderEnvelope;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Accusation;
import com.cluegame.model.Board;
import com.cluegame.model.Dice;
import com.cluegame.model.Game;
import com.cluegame.model.Room;
import com.cluegame.model.Square;
import com.cluegame.model.Suggestion;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the JavaFX GUI turn flow. Manages movement, suggestions,
 * accusations and turn advancement without touching console I/O.
 * Handles both human and AI player turns through the GUI.
 * @author Thanh Shaw
 */
public class GameController {

    private static final String[] SUSPECTS = {
        "Miss Scarlett", "Colonel Mustard", "Mrs White",
        "Reverend Green", "Mrs Peacock", "Professor Plum"
    };

    private static final String[] WEAPONS = {
        "Candlestick", "Dagger", "Lead Piping",
        "Revolver", "Rope", "Spanner"
    };

    private Board board;
    private Dice dice;
    private List<Player> players;
    private MurderEnvelope envelope;
    private Game game;
    private int currentPlayerIndex;
    private int remainingSteps;
    private boolean movementActive;
    private int previousRow;
    private int previousCol;
    private boolean gameOver;
    private Player winner;

    /**
     * Creates a controller for the given game state.
     * @param game the Game instance (provides board, dice, players, envelope,
     *             non-player suspects, weapon positions)
     */
    public GameController(Game game) {
        this.game = game;
        this.board = game.getBoard();
        this.dice = game.getDice();
        this.players = game.getPlayers();
        this.envelope = game.getMurderEnvelope();
        this.currentPlayerIndex = 0;
        this.remainingSteps = 0;
        this.movementActive = false;
        this.previousRow = -1;
        this.previousCol = -1;
        this.gameOver = false;
        this.winner = null;
    }

    // --- Human movement ---

    /**
     * Rolls the dice and starts the movement phase for the current player.
     * @return the dice roll total
     */
    public int rollDice() {
        int roll = dice.roll();
        remainingSteps = roll;
        movementActive = true;
        previousRow = -1;
        previousCol = -1;
        return roll;
    }

    /**
     * Returns the list of valid squares the current player can move to.
     * @return list of {row, col} arrays for valid destinations
     */
    public List<int[]> getValidMoves() {
        List<int[]> valid = new ArrayList<>();
        if (!movementActive || remainingSteps <= 0) return valid;

        Player current = getCurrentPlayer();
        if (current.getCurrentRoom() != null) return valid;

        int row = current.getRow();
        int col = current.getCol();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] d : directions) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (nr == previousRow && nc == previousCol) continue;
            if (board.isValidMove(row, col, nr, nc, players)) {
                valid.add(new int[]{nr, nc});
            }
        }
        return valid;
    }

    /**
     * Attempts to move the current player to the given row/col.
     * @param targetRow the target row
     * @param targetCol the target column
     * @return a MoveResult describing what happened
     */
    public MoveResult tryMove(int targetRow, int targetCol) {
        if (!movementActive || remainingSteps <= 0) return MoveResult.INVALID;

        List<int[]> valid = getValidMoves();
        boolean isValid = false;
        for (int[] v : valid) {
            if (v[0] == targetRow && v[1] == targetCol) {
                isValid = true;
                break;
            }
        }
        if (!isValid) return MoveResult.INVALID;

        Player current = getCurrentPlayer();
        previousRow = current.getRow();
        previousCol = current.getCol();
        current.moveTo(targetRow, targetCol);
        remainingSteps--;

        if (board.isDoor(targetRow, targetCol)) {
            // don't auto-enter — let the player choose
            return MoveResult.LANDED_ON_DOOR;
        }

        if (remainingSteps <= 0) movementActive = false;
        return MoveResult.MOVED;
    }

    /**
     * Ends the current player's movement early.
     */
    public void endMove() {
        movementActive = false;
        remainingSteps = 0;
    }

    // --- Room exit ---

    /**
     * Returns the list of exit options for a player currently in a room.
     * @return list of exit descriptions
     */
    public List<String> getRoomExitOptions() {
        Player current = getCurrentPlayer();
        Room room = current.getCurrentRoom();
        if (room == null) return new ArrayList<>();

        List<String> options = new ArrayList<>();
        for (Square door : room.getDoors()) {
            boolean blocked = isDoorBlocked(door, current);
            if (!blocked) {
                options.add("Exit via door at (" + door.getRow() + ", " + door.getCol() + ")");
            }
        }
        if (room.hasSecretPassage()) {
            options.add("Secret passage to " + room.getSecretPassage().getName());
        }
        return options;
    }

    /**
     * Exits the current player from their room via the chosen option index.
     * @param optionIndex the index from getRoomExitOptions()
     * @return description of what happened
     */
    public String exitRoom(int optionIndex) {
        Player current = getCurrentPlayer();
        Room room = current.getCurrentRoom();
        if (room == null) return "Not in a room.";

        List<String> options = getRoomExitOptions();
        if (optionIndex < 0 || optionIndex >= options.size()) return "Invalid option.";

        String chosen = options.get(optionIndex);
        if (chosen.startsWith("Secret passage")) {
            Room dest = room.getSecretPassage();
            current.leaveRoom();
            current.enterRoom(dest);
            return current.getName() + " uses the secret passage to " + dest.getName() + ".";
        }

        int doorIndex = 0;
        for (Square door : room.getDoors()) {
            if (!isDoorBlocked(door, current)) {
                if (doorIndex == optionIndex) {
                    current.leaveRoom();
                    current.moveTo(door.getRow(), door.getCol());
                    return current.getName() + " exits " + room.getName() + ".";
                }
                doorIndex++;
            }
        }
        return "Could not exit.";
    }

    private boolean isDoorBlocked(Square door, Player excluding) {
        for (Player p : players) {
            if (p != excluding && p.isActive() && p.getCurrentRoom() == null
                    && p.getRow() == door.getRow() && p.getCol() == door.getCol()) {
                return true;
            }
        }
        return false;
    }

    // --- Suggestions ---

    /**
     * Resolves a suggestion by going clockwise from the current player.
     * @param suggestion the suggestion to resolve
     * @return the result of the suggestion resolution
     */
    public SuggestionResult resolveSuggestion(Suggestion suggestion) {
        Player suggester = getCurrentPlayer();
        int startIdx = players.indexOf(suggester);
        moveSuspectToRoom(suggestion, suggester.getCurrentRoom());

        for (int i = 1; i < players.size(); i++) {
            int idx = (startIdx + i) % players.size();
            Player other = players.get(idx);
            List<Card> matches = other.getMatchingCards(suggestion);
            if (!matches.isEmpty()) {
                Card shown = matches.get(0);
                return new SuggestionResult(other.getName(), shown, true);
            }
        }
        return new SuggestionResult(null, null, false);
    }

    private void moveSuspectToRoom(Suggestion suggestion, Room room) {
        String suspectName = suggestion.getSuspect().getName();

        // try to move a player-controlled suspect
        for (Player p : players) {
            if (suspectName.toLowerCase().contains(p.getToken().toLowerCase())) {
                if (p.getCurrentRoom() != room) {
                    if (p.getCurrentRoom() != null) p.leaveRoom();
                    p.enterRoom(room);
                }
                // also move the weapon into the room
                game.moveWeaponToRoom(suggestion.getWeapon().getName(), room.getName());
                return;
            }
        }

        // suspect is not a player — move non-player suspect piece
        for (String token : game.getNonPlayerSuspects().keySet()) {
            if (suspectName.toLowerCase().contains(token.toLowerCase())) {
                game.moveNonPlayerSuspect(token, room);
                break;
            }
        }

        // move the weapon into the room
        game.moveWeaponToRoom(suggestion.getWeapon().getName(), room.getName());
    }

    // --- Accusations ---

    /**
     * Checks an accusation against the murder envelope.
     * @param accusation the accusation to check
     * @return true if the accusation is correct
     */
    public boolean checkAccusation(Accusation accusation) {
        Player accuser = getCurrentPlayer();
        boolean correct = envelope.verify(
                accusation.getSuspect(), accusation.getWeapon(), accusation.getRoom());

        if (correct) {
            winner = accuser;
            gameOver = true;
        } else {
            accuser.setActive(false);
            int activeCount = 0;
            Player lastActive = null;
            for (Player p : players) {
                if (p.isActive()) { activeCount++; lastActive = p; }
            }
            if (activeCount <= 1) { winner = lastActive; gameOver = true; }
        }
        return correct;
    }

    // --- AI turn ---

    /** Tracks which room the AI just left, to avoid re-entering immediately. */
    private String lastExitedRoom;

    /**
     * Runs a complete AI turn: movement, suggestion, accusation.
     * Returns a list of log messages describing what happened.
     * @return list of log messages for the AI turn
     */
    public List<String> runAITurn() {
        List<String> log = new ArrayList<>();
        Player current = getCurrentPlayer();
        if (!(current instanceof AIPlayer) || !current.isActive()) return log;

        AIPlayer ai = (AIPlayer) current;
        lastExitedRoom = null;
        log.add("--- " + ai.getName() + "'s turn ---");

        // movement phase
        if (ai.getCurrentRoom() != null) {
            Room room = ai.getCurrentRoom();
            lastExitedRoom = room.getName();

            // use secret passage only sometimes (1 in 3 chance),
            // to prevent bouncing between the same two rooms
            if (room.hasSecretPassage() && Math.random() < 0.33) {
                Room dest = room.getSecretPassage();
                ai.leaveRoom();
                ai.enterRoom(dest);
                log.add(ai.getName() + " uses the secret passage to "
                        + dest.getName() + ".");
                lastExitedRoom = null; // entered a new room via passage
            } else {
                // exit through a free door
                Square freeDoor = null;
                for (Square door : room.getDoors()) {
                    if (!isDoorBlocked(door, ai)) { freeDoor = door; break; }
                }
                if (freeDoor != null) {
                    ai.leaveRoom();
                    ai.moveTo(freeDoor.getRow(), freeDoor.getCol());
                    log.add(ai.getName() + " exits " + room.getName() + ".");
                    moveAITowardRoom(ai, log);
                } else {
                    log.add(ai.getName() + " stays in " + room.getName() + ".");
                }
            }
        } else {
            moveAITowardRoom(ai, log);
        }

        // suggestion phase
        if (ai.getCurrentRoom() != null && ai.isActive()) {
            Suggestion suggestion = ai.makeSuggestion();
            if (suggestion != null) {
                log.add(ai.getName() + " suggests: " + suggestion);
                SuggestionResult result = resolveSuggestion(suggestion);

                // log suspect movement from suggestion
                String suspectName = suggestion.getSuspect().getName();
                for (Player p : players) {
                    if (suspectName.toLowerCase().contains(p.getToken().toLowerCase())
                            && p != ai) {
                        log.add("  (" + p.getName() + "'s token moved to "
                                + ai.getCurrentRoom().getName()
                                + " by the suggestion)");
                        break;
                    }
                }

                if (result.isDisproved()) {
                    log.add(result.getDisproverName()
                            + " disproves the suggestion.");
                    ai.seeDisprovalCard(result.getShownCard(),
                            result.getDisproverName());
                } else {
                    log.add("No one could disprove the suggestion!");
                }

                // accusation phase
                if (ai.isActive() && !gameOver) {
                    Accusation accusation = ai.makeAccusation();
                    if (accusation != null) {
                        log.add(ai.getName() + " accuses: " + accusation);
                        boolean correct = checkAccusation(accusation);
                        if (correct) {
                            log.add(ai.getName() + " wins the game!");
                        } else {
                            log.add(ai.getName()
                                    + "'s accusation was WRONG! Eliminated.");
                        }
                    }
                }
            }
        }

        return log;
    }

    /**
     * Moves an AI player toward a room they haven't just exited,
     * using dice roll and step-by-step pathfinding.
     */
    private void moveAITowardRoom(AIPlayer ai, List<String> log) {
        int roll = dice.roll();
        log.add(ai.getName() + " rolled a " + roll + ".");

        for (int step = 0; step < roll; step++) {
            // if standing on a door to a NEW room, enter it
            if (board.isDoor(ai.getRow(), ai.getCol())) {
                Room room = board.getRoomAt(ai.getRow(), ai.getCol());
                boolean isOldRoom = lastExitedRoom != null
                        && room.getName().equals(lastExitedRoom);
                if (!isOldRoom) {
                    ai.enterRoom(room);
                    log.add(ai.getName() + " entered the "
                            + room.getName() + ".");
                    return;
                }
                // standing on old room's door — move away normally below
            }

            int[] dir = findBestDirection(ai);
            if (dir == null) break;
            ai.moveTo(ai.getRow() + dir[0], ai.getCol() + dir[1]);
        }

        // check final position
        if (board.isDoor(ai.getRow(), ai.getCol())) {
            Room room = board.getRoomAt(ai.getRow(), ai.getCol());
            if (lastExitedRoom == null
                    || !room.getName().equals(lastExitedRoom)) {
                ai.enterRoom(room);
                log.add(ai.getName() + " entered the " + room.getName() + ".");
            }
        }
    }

    /**
     * Finds the best direction for an AI player to move, heading toward
     * the nearest room door (excluding the room they just left).
     */
    private int[] findBestDirection(Player ai) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        Square target = findNearestDoor(ai);

        List<int[]> validDirs = new ArrayList<>();
        int[] bestDir = null;
        int bestDist = Integer.MAX_VALUE;

        for (int[] d : directions) {
            int nr = ai.getRow() + d[0];
            int nc = ai.getCol() + d[1];
            if (board.isValidMove(ai.getRow(), ai.getCol(), nr, nc, players)) {
                validDirs.add(d);
                if (target != null) {
                    int dist = Math.abs(nr - target.getRow())
                             + Math.abs(nc - target.getCol());
                    if (dist < bestDist) { bestDist = dist; bestDir = d; }
                }
            }
        }

        if (bestDir != null) return bestDir;
        if (!validDirs.isEmpty()) return validDirs.get(0);
        return null;
    }

    /**
     * Finds the nearest room door, excluding the room the AI just exited.
     * This prevents the AI from immediately re-entering the same room.
     */
    private Square findNearestDoor(Player ai) {
        Square nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Room room : board.getRooms().values()) {
            // skip doors to the room we just left
            if (lastExitedRoom != null
                    && room.getName().equals(lastExitedRoom)) {
                continue;
            }
            for (Square door : room.getDoors()) {
                int dist = Math.abs(ai.getRow() - door.getRow())
                         + Math.abs(ai.getCol() - door.getCol());
                if (dist < minDist) { minDist = dist; nearest = door; }
            }
        }
        // fallback: if no other room found, allow any door
        if (nearest == null) {
            for (Room room : board.getRooms().values()) {
                for (Square door : room.getDoors()) {
                    int dist = Math.abs(ai.getRow() - door.getRow())
                             + Math.abs(ai.getCol() - door.getCol());
                    if (dist < minDist) { minDist = dist; nearest = door; }
                }
            }
        }
        return nearest;
    }

    // --- Turn advancement ---

    /**
     * Advances to the next active player. Returns true if the next player
     * is human (needs interactive turn), false if AI (caller should run AI turn).
     */
    public void advanceToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());

        movementActive = false;
        remainingSteps = 0;
        previousRow = -1;
        previousCol = -1;
    }

    /**
     * Returns true if the current player is an AI player.
     */
    public boolean isCurrentPlayerAI() {
        return getCurrentPlayer() instanceof AIPlayer;
    }

    /**
     * Returns true if the current player is a human player.
     */
    public boolean isCurrentPlayerHuman() {
        return getCurrentPlayer() instanceof HumanPlayer;
    }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public List<Player> getPlayers() { return players; }
    public int getRemainingSteps() { return remainingSteps; }
    public boolean isMovementActive() { return movementActive; }
    public Board getBoard() { return board; }
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
    public MurderEnvelope getEnvelope() { return envelope; }
    public Game getGame() { return game; }

    public static String[] getSuspects() { return SUSPECTS; }
    public static String[] getWeapons() { return WEAPONS; }

    /**
     * Result of a movement attempt.
     */
    public enum MoveResult {
        MOVED, ENTERED_ROOM, LANDED_ON_DOOR, INVALID
    }

    /**
     * Enters the room at the current player's position (after landing on a door).
     * Call this when the player chooses to enter rather than walk past.
     */
    public void enterCurrentDoor() {
        Player current = getCurrentPlayer();
        Room room = board.getRoomAt(current.getRow(), current.getCol());
        if (room != null) {
            current.enterRoom(room);
            movementActive = false;
            remainingSteps = 0;
        }
    }

    /**
     * Result of a suggestion resolution.
     */
    public static class SuggestionResult {
        private final String disproverName;
        private final Card shownCard;
        private final boolean disproved;

        public SuggestionResult(String disproverName, Card shownCard, boolean disproved) {
            this.disproverName = disproverName;
            this.shownCard = shownCard;
            this.disproved = disproved;
        }

        public String getDisproverName() { return disproverName; }
        public Card getShownCard() { return shownCard; }
        public boolean isDisproved() { return disproved; }
    }
}
