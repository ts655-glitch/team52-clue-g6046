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

import java.util.List;
import java.util.Scanner;

/**
 * Represents a human-controlled player in Clue!
 * Movement, suggestions and accusations are made via console input.
 * @author Thanh Shaw
 */
public class HumanPlayer extends Player {

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

    private Scanner scanner;

    /**
     * Constructs a human player with a name, token and starting position.
     * @param name the player's name
     * @param token the player's token identifier
     * @param startRow starting row on the board
     * @param startCol starting column on the board
     */
    public HumanPlayer(String name, String token, int startRow, int startCol) {
        super(name, token, startRow, startCol);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Constructs a human player with a custom Scanner (useful for testing).
     * @param name the player's name
     * @param token the player's token identifier
     * @param startRow starting row on the board
     * @param startCol starting column on the board
     * @param scanner the Scanner to use for input
     */
    public HumanPlayer(String name, String token, int startRow, int startCol, Scanner scanner) {
        super(name, token, startRow, startCol);
        this.scanner = scanner;
    }

    /**
     * Takes a turn by rolling the dice and moving step-by-step through
     * corridors. The player chooses direction each step via console input.
     * If the player is in a room they may use a secret passage or exit
     * through a door before rolling.
     * @param board the game board
     * @param dice the dice to roll for movement
     * @param allPlayers all players (for corridor occupancy checks)
     */
    @Override
    public void takeTurn(Board board, Dice dice, List<Player> allPlayers) {
        System.out.println("\n--- " + getName() + "'s turn ---");
        System.out.println("Your hand: " + getHandSummary());

        // if in a room, offer secret passage or door exit
        if (getCurrentRoom() != null) {
            System.out.println("You are in the " + getCurrentRoom().getName() + ".");

            if (getCurrentRoom().hasSecretPassage()) {
                Room dest = getCurrentRoom().getSecretPassage();
                System.out.print("Use secret passage to " + dest.getName() + "? (Y/N): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    leaveRoom();
                    enterRoom(dest);
                    System.out.println("You used the secret passage to the " + dest.getName() + ".");
                    return;
                }
            }

            // offer to stay or choose an exit door
            List<Square> doors = getCurrentRoom().getDoors();
            System.out.println("Choose an option:");
            System.out.println("  0) Stay in " + getCurrentRoom().getName());
            for (int i = 0; i < doors.size(); i++) {
                Square d = doors.get(i);
                System.out.println("  " + (i + 1) + ") Exit via door at ("
                        + d.getRow() + ", " + d.getCol() + ")");
            }

            int choice = readInt(0, doors.size());
            if (choice == 0) {
                System.out.println("You stay in the " + getCurrentRoom().getName() + ".");
                return;
            }

            Square exitDoor = doors.get(choice - 1);
            if (isDoorBlocked(exitDoor, allPlayers)) {
                System.out.println("That door is blocked by another player. You stay in the room.");
                return;
            }

            leaveRoom();
            moveTo(exitDoor.getRow(), exitDoor.getCol());
        }

        // roll dice
        int roll = dice.roll();
        System.out.println("You rolled a " + roll + ".");

        int remaining = roll;
        String[] dirNames = {"N", "S", "E", "W"};
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, 1, -1};

        while (remaining > 0) {
            System.out.println("\nPosition: (" + getRow() + ", " + getCol()
                    + ") | Moves left: " + remaining);

            // show which directions are valid
            StringBuilder options = new StringBuilder("  Directions: ");
            boolean hasMove = false;
            for (int i = 0; i < 4; i++) {
                if (board.isValidMove(getRow(), getCol(),
                        getRow() + dRow[i], getCol() + dCol[i], allPlayers)) {
                    options.append("[").append(dirNames[i]).append("] ");
                    hasMove = true;
                }
            }

            // if standing on a door, offer room entry as an option
            boolean onDoor = board.isDoor(getRow(), getCol());
            if (onDoor) {
                Room doorRoom = board.getRoomAt(getRow(), getCol());
                options.append("[R = enter ").append(doorRoom.getName()).append("] ");
            }
            options.append("[P = pass]");

            if (!hasMove && !onDoor) {
                System.out.println("No valid moves available. Turn ends.");
                break;
            }
            System.out.println(options);
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim().toUpperCase();

            // handle room entry
            if (input.equals("R") && onDoor) {
                Room doorRoom = board.getRoomAt(getRow(), getCol());
                enterRoom(doorRoom);
                System.out.println("You entered the " + doorRoom.getName() + ".");
                return; // entering a room ends the move
            }

            // handle pass
            if (input.equals("P")) {
                System.out.println("You choose to stop moving.");
                break;
            }

            int newRow = getRow();
            int newCol = getCol();
            switch (input) {
                case "N": newRow--; break;
                case "S": newRow++; break;
                case "E": newCol++; break;
                case "W": newCol--; break;
                default:
                    System.out.println("Invalid input. Use N/S/E/W, R to enter a room, or P to pass.");
                    continue;
            }

            if (board.isValidMove(getRow(), getCol(), newRow, newCol, allPlayers)) {
                moveTo(newRow, newCol);
                remaining--;
            } else {
                System.out.println("You can't move there.");
            }
        }

        // after all steps used, check if ended on a door
        if (remaining == 0 && board.isDoor(getRow(), getCol())) {
            Room doorRoom = board.getRoomAt(getRow(), getCol());
            System.out.println("\nYou finished on the door to " + doorRoom.getName() + ".");
            System.out.print("Type R to enter, or anything else to stay outside: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("R")) {
                enterRoom(doorRoom);
                System.out.println("You entered the " + doorRoom.getName() + ".");
            }
        }
    }

    /**
     * Lets the human player make a suggestion via console input.
     * The room is automatically set to the room the player is currently in.
     * The player picks a suspect and weapon from numbered menus.
     * @return the Suggestion, or null if the player declines
     */
    @Override
    public Suggestion makeSuggestion() {
        if (getCurrentRoom() == null) return null;

        System.out.print("\nMake a suggestion? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return null;

        String roomName = getCurrentRoom().getName();
        System.out.println("Room: " + roomName + " (automatic)");

        System.out.println("Choose a suspect:");
        for (int i = 0; i < SUSPECTS.length; i++) {
            System.out.println("  " + (i + 1) + ") " + SUSPECTS[i]);
        }
        int suspectIdx = readInt(1, SUSPECTS.length) - 1;

        System.out.println("Choose a weapon:");
        for (int i = 0; i < WEAPONS.length; i++) {
            System.out.println("  " + (i + 1) + ") " + WEAPONS[i]);
        }
        int weaponIdx = readInt(1, WEAPONS.length) - 1;

        return new Suggestion(
                new SuspectCard(SUSPECTS[suspectIdx]),
                new WeaponCard(WEAPONS[weaponIdx]),
                new RoomCard(roomName)
        );
    }

    /**
     * Asks the human player if they want to make an accusation. If yes,
     * they pick a suspect, weapon and room (can be any room, not just current).
     * @return the Accusation, or null if the player declines
     */
    @Override
    public Accusation makeAccusation() {
        System.out.print("\nMake an accusation? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return null;

        System.out.println("WARNING: A wrong accusation will eliminate you from the game!");
        System.out.print("Are you sure? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return null;

        System.out.println("Choose the murderer:");
        for (int i = 0; i < SUSPECTS.length; i++) {
            System.out.println("  " + (i + 1) + ") " + SUSPECTS[i]);
        }
        int suspectIdx = readInt(1, SUSPECTS.length) - 1;

        System.out.println("Choose the murder weapon:");
        for (int i = 0; i < WEAPONS.length; i++) {
            System.out.println("  " + (i + 1) + ") " + WEAPONS[i]);
        }
        int weaponIdx = readInt(1, WEAPONS.length) - 1;

        System.out.println("Choose the room:");
        for (int i = 0; i < ROOMS.length; i++) {
            System.out.println("  " + (i + 1) + ") " + ROOMS[i]);
        }
        int roomIdx = readInt(1, ROOMS.length) - 1;

        return new Accusation(
                new SuspectCard(SUSPECTS[suspectIdx]),
                new WeaponCard(WEAPONS[weaponIdx]),
                new RoomCard(ROOMS[roomIdx])
        );
    }

    /**
     * When this player must disprove a suggestion, lets them choose which
     * matching card to show. If only one card matches it is shown automatically.
     * @param matchingCards cards in hand that match the suggestion
     * @param askerName the player who made the suggestion
     * @return the card chosen to reveal
     */
    @Override
    public Card chooseSuggestionCard(List<Card> matchingCards, String askerName) {
        if (matchingCards.size() == 1) {
            System.out.println("You show " + matchingCards.get(0) + " to " + askerName + ".");
            return matchingCards.get(0);
        }

        System.out.println("\n>>> " + askerName
                + " made a suggestion. You must show one of these cards:");
        for (int i = 0; i < matchingCards.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + matchingCards.get(i));
        }
        System.out.println("Which card do you show to " + askerName + "?");
        int choice = readInt(1, matchingCards.size()) - 1;
        System.out.println("You show " + matchingCards.get(choice) + " to " + askerName + ".");
        return matchingCards.get(choice);
    }

    /**
     * Called when another player shows this player a card to disprove
     * their suggestion. Displays the card to the console.
     * @param card the card being shown
     * @param fromPlayerName the name of the player showing the card
     */
    @Override
    public void seeDisprovalCard(Card card, String fromPlayerName) {
        System.out.println(fromPlayerName + " shows you: " + card);
    }

    /**
     * Reads an integer from the console within the given range (inclusive).
     * Keeps prompting until the player enters a valid number.
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return the valid integer entered by the player
     */
    private int readInt(int min, int max) {
        while (true) {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Checks if a door square is occupied by another player in the corridor.
     * @param door the door square to check
     * @param allPlayers all players in the game
     * @return true if another player is standing on that door square
     */
    private boolean isDoorBlocked(Square door, List<Player> allPlayers) {
        for (Player p : allPlayers) {
            if (p != this && p.isActive() && p.getCurrentRoom() == null
                    && p.getRow() == door.getRow() && p.getCol() == door.getCol()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a short comma-separated summary of the player's hand.
     * @return hand contents as a single line string
     */
    private String getHandSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getHand().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(getHand().get(i).getName());
        }
        return sb.toString();
    }
}
