package com.cluegame.players;

import com.cluegame.cards.Card;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Accusation;
import com.cluegame.model.Board;
import com.cluegame.model.Dice;
import com.cluegame.model.Room;
import com.cluegame.model.Square;
import com.cluegame.model.Suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an autonomous AI player in Clue!
 * Moves toward rooms, makes random suggestions and uses a DetectiveNotepad
 * to track seen cards and deduce when to make an accusation.
 * @author Thanh Shaw
 */
public class AIPlayer extends Player {

    private static final String[] SUSPECTS = {
        "Miss Scarlett", "Colonel Mustard", "Mrs White",
        "Reverend Green", "Mrs Peacock", "Professor Plum"
    };

    private static final String[] WEAPONS = {
        "Candlestick", "Dagger", "Lead Piping",
        "Revolver", "Rope", "Spanner"
    };

    private static final String[] ROOMS = {
        "Kitchen", "Ballroom", "Conservatory", "Billiard Room",
        "Library", "Study", "Hall", "Lounge", "Dining Room"
    };

    private Random random;
    private DetectiveNotepad notepad;
    private String lastSuggestedSuspect;
    private String lastSuggestedWeapon;
    private Suggestion undisprovedSuggestion;

    /**
     * Constructs an AI player with a name, token and starting position.
     * @param name the player's name
     * @param token the player's token identifier
     * @param startRow starting row on the board
     * @param startCol starting column on the board
     */
    public AIPlayer(String name, String token, int startRow, int startCol) {
        super(name, token, startRow, startCol);
        this.random = new Random();
        this.notepad = new DetectiveNotepad();
        this.lastSuggestedSuspect = null;
        this.lastSuggestedWeapon = null;
        this.undisprovedSuggestion = null;
    }

    /**
     * Overrides addCard to also record the card in the detective notepad.
     * Cards in the AI's own hand cannot be in the murder envelope.
     * @param card the card to add
     */
    @Override
    public void addCard(Card card) {
        super.addCard(card);
        notepad.markSeen(card);
    }

    /**
     * Takes a turn by rolling the dice and moving toward the nearest room.
     * If already in a room, the AI will sometimes use a secret passage
     * or otherwise exit through a random available door and keep moving.
     * @param board the game board
     * @param dice the dice to roll
     * @param allPlayers all players for corridor occupancy checks
     */
    @Override
    public void takeTurn(Board board, Dice dice, List<Player> allPlayers) {
        System.out.println(getName() + " (AI) is taking their turn.");

        // if in a room: use secret passage or exit
        if (getCurrentRoom() != null) {
            // use secret passage if it leads to an unvisited room, or randomly
            if (getCurrentRoom().hasSecretPassage()) {
                Room dest = getCurrentRoom().getSecretPassage();
                boolean destUnvisited = !notepad.getSeenCards().contains(dest.getName());
                if (destUnvisited || random.nextInt(3) == 0) {
                    System.out.println(getName() + " uses the secret passage to "
                            + dest.getName() + ".");
                    leaveRoom();
                    enterRoom(dest);
                    return;
                }
            }

            // try to exit through a random unblocked door
            Square exitDoor = findFreeDoor(getCurrentRoom(), allPlayers);
            if (exitDoor == null) {
                System.out.println(getName() + " stays in "
                        + getCurrentRoom().getName() + " (all doors blocked).");
                return;
            }

            System.out.println(getName() + " exits " + getCurrentRoom().getName() + ".");
            leaveRoom();
            moveTo(exitDoor.getRow(), exitDoor.getCol());
        }

        // roll dice and move step by step
        int roll = dice.roll();
        System.out.println(getName() + " rolled a " + roll + ".");

        for (int step = 0; step < roll; step++) {
            // if on a door, enter the room
            if (board.isDoor(getRow(), getCol())) {
                Room room = board.getRoomAt(getRow(), getCol());
                enterRoom(room);
                System.out.println(getName() + " entered the " + room.getName() + ".");
                return;
            }

            // pick best direction toward nearest room door
            int[] dir = findBestDirection(board, allPlayers);
            if (dir == null) {
                System.out.println(getName() + " is stuck — no valid moves.");
                break;
            }
            moveTo(getRow() + dir[0], getCol() + dir[1]);
        }

        // if finished on a door, enter the room
        if (board.isDoor(getRow(), getCol())) {
            Room room = board.getRoomAt(getRow(), getCol());
            enterRoom(room);
            System.out.println(getName() + " entered the " + room.getName() + ".");
        } else {
            System.out.println(getName() + " is at (" + getRow() + ", " + getCol() + ").");
        }
    }

    /**
     * Makes a suggestion using the current room. Prioritises unseen suspects
     * and weapons to gather new information, avoids repeating the exact same
     * suggestion from the previous turn, and targets the deduced envelope
     * card when a category is fully narrowed.
     * @return a Suggestion using the current room, or null if not in a room
     */
    @Override
    public Suggestion makeSuggestion() {
        if (getCurrentRoom() == null) return null;

        String suspect = pickBest(SUSPECTS, lastSuggestedSuspect);
        String weapon = pickBest(WEAPONS, lastSuggestedWeapon);
        String roomName = getCurrentRoom().getName();

        lastSuggestedSuspect = suspect;
        lastSuggestedWeapon = weapon;

        return new Suggestion(
                new SuspectCard(suspect),
                new WeaponCard(weapon),
                new RoomCard(roomName)
        );
    }

    /**
     * Picks the best name to suggest from a category. Priority order:
     * 1. The deduced envelope card (if exactly one unseen remains)
     * 2. An unseen name that was not suggested last turn
     * 3. Any unseen name
     * 4. The deduced missing card (if known)
     * 5. A random name as a last resort
     * @param names all names in the category
     * @param lastSuggested the name suggested last turn (to avoid repeats)
     * @return the chosen name
     */
    private String pickBest(String[] names, String lastSuggested) {
        List<String> unseen = new ArrayList<>();
        for (String name : names) {
            if (!notepad.getSeenCards().contains(name)) {
                unseen.add(name);
            }
        }

        // if exactly one unseen, that is the deduced answer — always use it
        if (unseen.size() == 1) {
            return unseen.get(0);
        }

        // prefer unseen names that were not suggested last turn
        if (unseen.size() > 1) {
            List<String> fresh = new ArrayList<>();
            for (String name : unseen) {
                if (!name.equals(lastSuggested)) fresh.add(name);
            }
            if (!fresh.isEmpty()) {
                return fresh.get(random.nextInt(fresh.size()));
            }
            return unseen.get(random.nextInt(unseen.size()));
        }

        // all seen — use the deduced missing card if available
        String deduced = notepad.deduceMissing(names);
        if (deduced != null) return deduced;

        // last resort — random (should rarely happen)
        return names[random.nextInt(names.length)];
    }

    /**
     * Records a suggestion that no one could disprove. The AI treats this
     * as a strong accusation candidate on future turns.
     * @param suggestion the undisproved suggestion
     */
    public void setUndisprovedSuggestion(Suggestion suggestion) {
        this.undisprovedSuggestion = suggestion;
    }

    /**
     * Makes an accusation if the AI has enough information. Checks in order:
     * 1. Fully deduced (all three categories narrowed to one) — accuse.
     * 2. Has an undisproved suggestion — 50% chance to accuse with it.
     * 3. Two of three categories deduced — 50% chance to guess the third.
     * 4. Otherwise, do not accuse.
     * @return an Accusation if the AI is ready, or null
     */
    @Override
    public Accusation makeAccusation() {
        String suspect = notepad.deduceMissing(SUSPECTS);
        String weapon = notepad.deduceMissing(WEAPONS);
        String room = notepad.deduceMissing(ROOMS);

        // fully deduced — accuse with certainty
        if (suspect != null && weapon != null && room != null) {
            return new Accusation(
                    new SuspectCard(suspect),
                    new WeaponCard(weapon),
                    new RoomCard(room)
            );
        }

        // undisproved suggestion — strong candidate, 50% chance to pounce
        if (undisprovedSuggestion != null && random.nextInt(2) == 0) {
            Accusation acc = new Accusation(
                    undisprovedSuggestion.getSuspect(),
                    undisprovedSuggestion.getWeapon(),
                    undisprovedSuggestion.getRoom()
            );
            undisprovedSuggestion = null; // only try once
            return acc;
        }

        // calculated risk: if 2 of 3 categories are deduced, guess the third
        if (random.nextInt(2) == 0) {
            int deduced = 0;
            if (suspect != null) deduced++;
            if (weapon != null) deduced++;
            if (room != null) deduced++;

            if (deduced == 2) {
                if (suspect == null) suspect = pickBest(SUSPECTS, null);
                if (weapon == null) weapon = pickBest(WEAPONS, null);
                if (room == null) room = pickBest(ROOMS, null);

                return new Accusation(
                        new SuspectCard(suspect),
                        new WeaponCard(weapon),
                        new RoomCard(room)
                );
            }
        }

        return null;
    }

    /**
     * Automatically shows the first matching card to the suggesting player.
     * @param matchingCards cards in hand that match the suggestion
     * @param askerName the player who made the suggestion
     * @return the first matching card
     */
    @Override
    public Card chooseSuggestionCard(List<Card> matchingCards, String askerName) {
        // just show the first matching card
        return matchingCards.get(0);
    }

    /**
     * Records a shown card in the detective notepad for future deduction.
     * @param card the card being shown
     * @param fromPlayerName the player showing the card
     */
    @Override
    public void seeDisprovalCard(Card card, String fromPlayerName) {
        notepad.markSeen(card);
    }

    /**
     * Returns the AI's detective notepad.
     * @return the notepad tracking seen cards
     */
    public DetectiveNotepad getNotepad() {
        return notepad;
    }

    /**
     * Picks the best direction to move by trying to get closer to the
     * nearest room door. If no direction gets closer, picks a random
     * valid one instead.
     * @param board the game board
     * @param allPlayers all players for occupancy checks
     * @return int array {dRow, dCol} for the chosen step, or null if stuck
     */
    private int[] findBestDirection(Board board, List<Player> allPlayers) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // find nearest door to head toward
        Square target = findNearestDoor(board);

        // try each direction and pick the one that gets closest
        List<int[]> validDirs = new ArrayList<>();
        int[] bestDir = null;
        int bestDist = Integer.MAX_VALUE;

        for (int[] d : directions) {
            int nr = getRow() + d[0];
            int nc = getCol() + d[1];
            if (board.isValidMove(getRow(), getCol(), nr, nc, allPlayers)) {
                validDirs.add(d);
                if (target != null) {
                    int dist = Math.abs(nr - target.getRow())
                             + Math.abs(nc - target.getCol());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestDir = d;
                    }
                }
            }
        }

        if (bestDir != null) return bestDir;
        if (!validDirs.isEmpty()) return validDirs.get(random.nextInt(validDirs.size()));
        return null;
    }

    /**
     * Finds the nearest room door on the board, preferring rooms the AI
     * has not yet visited (to gather information about more rooms).
     * Falls back to the overall nearest door if all rooms have been visited.
     * @param board the game board
     * @return the nearest door Square, or null if none found
     */
    private Square findNearestDoor(Board board) {
        Square nearestUnvisited = null;
        int minUnvisited = Integer.MAX_VALUE;
        Square nearestAny = null;
        int minAny = Integer.MAX_VALUE;

        for (Room room : board.getRooms().values()) {
            boolean visited = notepad.getSeenCards().contains(room.getName());
            for (Square door : room.getDoors()) {
                int dist = Math.abs(getRow() - door.getRow())
                         + Math.abs(getCol() - door.getCol());
                if (dist < minAny) {
                    minAny = dist;
                    nearestAny = door;
                }
                if (!visited && dist < minUnvisited) {
                    minUnvisited = dist;
                    nearestUnvisited = door;
                }
            }
        }
        // prefer unvisited rooms to gather new information
        return nearestUnvisited != null ? nearestUnvisited : nearestAny;
    }

    /**
     * Finds a random unblocked door in the given room.
     * @param room the room to exit from
     * @param allPlayers all players for occupancy checks
     * @return a free door Square, or null if all doors are blocked
     */
    private Square findFreeDoor(Room room, List<Player> allPlayers) {
        List<Square> free = new ArrayList<>();
        for (Square door : room.getDoors()) {
            boolean blocked = false;
            for (Player p : allPlayers) {
                if (p != this && p.isActive() && p.getCurrentRoom() == null
                        && p.getRow() == door.getRow()
                        && p.getCol() == door.getCol()) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) free.add(door);
        }
        return free.isEmpty() ? null : free.get(random.nextInt(free.size()));
    }
}
