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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private SuggestionCardChooser suggestionCardChooser;

    // tracks the last room each AI exited, to prevent immediate re-entry
    private java.util.Map<String, String> aiLastExitedRoom = new java.util.HashMap<>();

    @FunctionalInterface
    public interface SuggestionCardChooser {
        Card chooseCard(Player disprover, List<Card> matchingCards, String askerName);
    }

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
        this.currentPlayerIndex = game.getCurrentPlayerIndex();
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
                Card shown;
                if (other instanceof HumanPlayer && suggestionCardChooser != null) {
                    shown = suggestionCardChooser.chooseCard(
                            other, matches, suggester.getName());
                } else if (other instanceof HumanPlayer) {
                    // Avoid console blocking in GUI/tests if no chooser is set.
                    shown = matches.get(0);
                } else {
                    shown = other.chooseSuggestionCard(matches, suggester.getName());
                }
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
            // check if any active players remain
            int activeCount = 0;
            for (Player p : players) {
                if (p.isActive()) activeCount++;
            }
            if (activeCount == 0) {
                // everyone eliminated — no one solved the mystery
                winner = null;
                gameOver = true;
            }
            // if one player remains, the game continues — they must still
            // make a correct accusation to win (standard Clue rules)
        }
        return correct;
    }

    // --- AI turn ---

    /** Current AI's last exited room for the active turn. */
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
        // restore this AI's persisted last-exited room
        lastExitedRoom = aiLastExitedRoom.get(ai.getName());
        log.add("--- " + ai.getName() + "'s turn ---");

        Accusation earlyAccusation = ai.makeAccusation();
        if (earlyAccusation != null) {
            handleAIAccusation(ai, earlyAccusation, log);
            return log;
        }

        // movement phase
        if (ai.getCurrentRoom() != null) {
            Room room = ai.getCurrentRoom();
            lastExitedRoom = room.getName();
            aiLastExitedRoom.put(ai.getName(), lastExitedRoom);

            // use secret passage only sometimes (1 in 3 chance),
            // to prevent bouncing between the same two rooms
            if (room.hasSecretPassage() && Math.random() < 0.33) {
                Room dest = room.getSecretPassage();
                ai.leaveRoom();
                ai.enterRoom(dest);
                log.add(ai.getName() + " uses the secret passage to "
                        + dest.getName() + ".");
                lastExitedRoom = null;
                aiLastExitedRoom.remove(ai.getName());
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
            // Track exploration separately from card knowledge:
            // entering a room does not mean the AI has seen that room card.
            ai.markVisitedRoom(ai.getCurrentRoom().getName());

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
                    // strong signal — save as a future accusation candidate
                    ai.setUndisprovedSuggestion(suggestion);
                }

                // accusation phase
                if (ai.isActive() && !gameOver) {
                    Accusation accusation = ai.makeAccusation();
                    if (accusation != null) {
                        handleAIAccusation(ai, accusation, log);
                    }
                }
            }
        }

        return log;
    }

    private void handleAIAccusation(AIPlayer ai, Accusation accusation,
                                    List<String> log) {
        log.add(ai.getName() + " accuses: " + accusation);
        boolean correct = checkAccusation(accusation);
        if (correct) {
            log.add(ai.getName() + " wins the game!");
        } else {
            log.add(ai.getName() + "'s accusation was WRONG! Eliminated.");
        }
    }

    /**
     * Moves an AI player toward a room they haven't just exited,
     * using dice roll and step-by-step pathfinding.
     */
    private void moveAITowardRoom(AIPlayer ai, List<String> log) {
        int roll = dice.roll();
        log.add(ai.getName() + " rolled a " + roll + ".");

        int startRow = ai.getRow();
        int startCol = ai.getCol();

        for (int step = 0; step < roll; step++) {
            // if standing on a door to a NEW room, enter it
            if (board.isDoor(ai.getRow(), ai.getCol())) {
                Room room = board.getRoomAt(ai.getRow(), ai.getCol());
                boolean isOldRoom = lastExitedRoom != null
                        && room.getName().equals(lastExitedRoom);
                if (!isOldRoom) {
                    ai.enterRoom(room);
                    aiLastExitedRoom.remove(ai.getName());
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
                aiLastExitedRoom.remove(ai.getName());
                log.add(ai.getName() + " entered the " + room.getName() + ".");
                return;
            }
        }

        // safety valve: if the AI made no progress (still at start position
        // or still on the old room's door), clear the lastExitedRoom restriction
        // so next turn it can enter the nearest room even if it's the old one
        if (ai.getRow() == startRow && ai.getCol() == startCol) {
            aiLastExitedRoom.remove(ai.getName());
            lastExitedRoom = null;
        }

        log.add(ai.getName() + " ends the turn at ("
                + ai.getRow() + ", " + ai.getCol() + ").");
    }

    /**
     * Finds the next step for an AI player to move, following a shortest
     * reachable path toward a room door rather than using a greedy
     * Manhattan-distance heuristic.
     */
    private int[] findBestDirection(Player ai) {
        if (ai instanceof AIPlayer) {
            AIPlayer aiPlayer = (AIPlayer) ai;
            String confirmedRoom = aiPlayer.getConfirmedRoomName();
            if (confirmedRoom != null) {
                int[] step = findPathStepToRoom(ai, confirmedRoom);
                if (step != null) return step;
            }

            List<Square> evidenceDoors = new ArrayList<>();
            for (Room room : board.getRooms().values()) {
                if (aiPlayer.couldRoomBeEnvelope(room.getName())) {
                    evidenceDoors.addAll(room.getDoors());
                }
            }
            int[] step = findPathStepToAnyDoor(ai, evidenceDoors);
            if (step != null) return step;
        }

        List<Square> unseenDoors = new ArrayList<>();
        List<Square> seenDoors = new ArrayList<>();
        List<Square> fallbackDoors = new ArrayList<>();

        Set<String> visitedRooms = Collections.emptySet();
        if (ai instanceof AIPlayer) {
            visitedRooms = ((AIPlayer) ai).getVisitedRooms();
        }

        for (Room room : board.getRooms().values()) {
            boolean isLastExited = lastExitedRoom != null
                    && room.getName().equals(lastExitedRoom);
            boolean visited = visitedRooms.contains(room.getName());

            for (Square door : room.getDoors()) {
                fallbackDoors.add(door);
                if (isLastExited) {
                    continue;
                }
                if (visited) {
                    seenDoors.add(door);
                } else {
                    unseenDoors.add(door);
                }
            }
        }

        int[] step = findPathStepToAnyDoor(ai, unseenDoors);
        if (step != null) return step;

        step = findPathStepToAnyDoor(ai, seenDoors);
        if (step != null) return step;

        step = findPathStepToAnyDoor(ai, fallbackDoors);
        if (step != null) return step;

        return findAnyValidDirection(ai);
    }

    private int[] findPathStepToRoom(Player ai, String roomName) {
        Room targetRoom = board.getRoom(roomName);
        if (targetRoom == null) return null;
        return findPathStepToAnyDoor(ai, new ArrayList<>(targetRoom.getDoors()));
    }

    /**
     * Runs a breadth-first search from the AI's current position and returns
     * the first step on a shortest reachable path to any target door.
     */
    private int[] findPathStepToAnyDoor(Player ai, List<Square> targets) {
        if (targets.isEmpty()) return null;

        Set<String> targetKeys = new HashSet<>();
        for (Square door : targets) {
            targetKeys.add(door.getRow() + "," + door.getCol());
        }

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean[][] visited = new boolean[Board.ROWS][Board.COLS];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        int startRow = ai.getRow();
        int startCol = ai.getCol();
        visited[startRow][startCol] = true;

        List<int[]> firstMoves = new ArrayList<>();
        for (int[] d : directions) {
            int nr = startRow + d[0];
            int nc = startCol + d[1];
            if (board.isValidMove(startRow, startCol, nr, nc, players)) {
                firstMoves.add(new int[]{nr, nc, d[0], d[1]});
            }
        }
        Collections.shuffle(firstMoves);

        for (int[] move : firstMoves) {
            int row = move[0];
            int col = move[1];
            visited[row][col] = true;
            if (targetKeys.contains(row + "," + col)) {
                return new int[]{move[2], move[3]};
            }
            queue.add(move);
        }

        while (!queue.isEmpty()) {
            int[] state = queue.removeFirst();
            int row = state[0];
            int col = state[1];
            int firstDr = state[2];
            int firstDc = state[3];

            for (int[] d : directions) {
                int nr = row + d[0];
                int nc = col + d[1];
                if (nr < 0 || nr >= Board.ROWS || nc < 0 || nc >= Board.COLS) {
                    continue;
                }
                if (visited[nr][nc]) {
                    continue;
                }
                if (!board.isValidMove(row, col, nr, nc, players)) {
                    continue;
                }
                visited[nr][nc] = true;
                if (targetKeys.contains(nr + "," + nc)) {
                    return new int[]{firstDr, firstDc};
                }
                queue.add(new int[]{nr, nc, firstDr, firstDc});
            }
        }

        return null;
    }

    /**
     * Falls back to any valid single-step move if no route to a door is found.
     */
    private int[] findAnyValidDirection(Player ai) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<int[]> validDirs = new ArrayList<>();

        for (int[] d : directions) {
            int nr = ai.getRow() + d[0];
            int nc = ai.getCol() + d[1];
            if (board.isValidMove(ai.getRow(), ai.getCol(), nr, nc, players)) {
                validDirs.add(d);
            }
        }

        if (validDirs.isEmpty()) {
            return null;
        }
        return validDirs.get((int) (Math.random() * validDirs.size()));
    }

    // --- Turn advancement ---

    /**
     * Advances to the next active player. Returns true if the next player
     * is human (needs interactive turn), false if AI (caller should run AI turn).
     */
    public void advanceToNextPlayer() {
        int activeCount = 0;
        for (Player p : players) {
            if (p.isActive()) activeCount++;
        }
        if (activeCount == 0) {
            winner = null;
            gameOver = true;
            movementActive = false;
            remainingSteps = 0;
            previousRow = -1;
            previousCol = -1;
            return;
        }

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
    public void setSuggestionCardChooser(SuggestionCardChooser chooser) {
        this.suggestionCardChooser = chooser;
    }

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
