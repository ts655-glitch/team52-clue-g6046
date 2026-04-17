package com.cluegame.model;

import com.cluegame.cards.Card;
import com.cluegame.cards.Deck;
import com.cluegame.cards.MurderEnvelope;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Top-level game controller. Manages turn order, game state and win condition.
 * Handles movement, suggestions, suggestion resolution and accusations.
 * @author Thanh Shaw
 */
public class Game {

    private static final String[] CHARACTER_NAMES = {
        "Miss Scarlett", "Colonel Mustard", "Mrs White",
        "Reverend Green", "Mrs Peacock", "Professor Plum"
    };

    private static final String[] TOKEN_NAMES = {
        "Scarlett", "Mustard", "White", "Green", "Peacock", "Plum"
    };

    // starting squares for up to 6 players [row][col]
    private static final int[][] START_POSITIONS = {
        {0, 16}, {7, 24}, {23, 16}, {23, 7}, {18, 0}, {5, 0}
    };

    private Board board;
    private Deck deck;
    private Dice dice;
    private MurderEnvelope murderEnvelope;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;
    private int turnCount;
    private Player winner;
    private boolean multipleHumans;
    private Scanner scanner;

    /**
     * Constructs a new Game with the given list of players.
     * @param players list of players (human and/or AI)
     */
    public Game(List<Player> players) {
        this(players, new Scanner(System.in));
    }

    /**
     * Constructs a new Game with the given list of players and a shared Scanner.
     * @param players list of players (human and/or AI)
     * @param scanner the Scanner for reading console input
     */
    public Game(List<Player> players, Scanner scanner) {
        this.players = players;
        this.board = new Board();
        this.dice = new Dice();
        this.murderEnvelope = new MurderEnvelope();
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.turnCount = 0;
        this.winner = null;
        this.scanner = scanner;

        // count human players to know if we need handoff screens
        int humanCount = 0;
        for (Player p : players) {
            if (p instanceof HumanPlayer) humanCount++;
        }
        this.multipleHumans = humanCount > 1;
    }

    /**
     * Runs the full game from start to finish. Sets up the game, then
     * loops through player turns until someone wins or all human players
     * are eliminated. Prints the board at the start of each turn.
     */
    public void run() {
        startGame();

        System.out.println("\n====================================");
        System.out.println("       CLUE! — THE GAME BEGINS      ");
        System.out.println("====================================");
        System.out.println(players.size() + " players. Find the murderer, weapon and room!");
        System.out.println("Type directions (N/S/E/W) to move. Good luck!\n");

        // show each human player their hand privately
        for (Player p : players) {
            if (p instanceof HumanPlayer) {
                if (multipleHumans) {
                    clearScreen();
                    System.out.println(">>> " + p.getName()
                            + ", press Enter to see your cards...");
                    scanner.nextLine();
                }
                System.out.println(p.getName() + ", your cards are:");
                for (Card card : p.getHand()) {
                    System.out.println("  " + card);
                }
                System.out.println();
                if (multipleHumans) {
                    System.out.println("Memorise your cards, then press Enter to continue.");
                    scanner.nextLine();
                }
            }
        }

        // main game loop — cap at 200 turns to prevent infinite AI-only games
        int maxTurns = 200;
        while (!gameOver && turnCount < maxTurns) {
            printStatus();
            nextTurn();

            // check if all human players are eliminated
            if (!gameOver && allHumansEliminated()) {
                System.out.println("\nAll human players have been eliminated.");
                System.out.println("The game ends — the mystery remains unsolved!");
                gameOver = true;
            }
        }

        if (turnCount >= maxTurns && !gameOver) {
            System.out.println("\nGame reached the maximum of " + maxTurns + " turns.");
            gameOver = true;
        }

        // game over — print the solution
        printGameOver();
    }

    /**
     * Starts the game by shuffling the deck, sealing the murder envelope,
     * dealing remaining cards to players, and placing tokens on start squares.
     */
    public void startGame() {
        deck.shuffle();

        // pick one of each card type for the envelope and remove from deck
        SuspectCard envelopeSuspect = null;
        WeaponCard envelopeWeapon = null;
        RoomCard envelopeRoom = null;

        List<Card> allCards = deck.getCards();
        List<Card> toRemove = new ArrayList<>();

        for (Card card : allCards) {
            if (envelopeSuspect == null && card instanceof SuspectCard) {
                envelopeSuspect = (SuspectCard) card;
                toRemove.add(card);
            } else if (envelopeWeapon == null && card instanceof WeaponCard) {
                envelopeWeapon = (WeaponCard) card;
                toRemove.add(card);
            } else if (envelopeRoom == null && card instanceof RoomCard) {
                envelopeRoom = (RoomCard) card;
                toRemove.add(card);
            }
            if (envelopeSuspect != null && envelopeWeapon != null && envelopeRoom != null) {
                break;
            }
        }

        allCards.removeAll(toRemove);
        murderEnvelope.seal(envelopeSuspect, envelopeWeapon, envelopeRoom);

        // deal the remaining 18 cards to players
        List<List<Card>> hands = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            for (Card card : hands.get(i)) {
                players.get(i).addCard(card);
            }
        }

        // place each player on their starting square
        for (int i = 0; i < players.size(); i++) {
            players.get(i).moveTo(START_POSITIONS[i][0], START_POSITIONS[i][1]);
        }
    }

    /**
     * Prints the current board state with player positions and a turn header.
     * Called at the start of each turn so players can see where everyone is.
     */
    /**
     * Prints the current board state with player positions and a turn header.
     * If multiple humans are playing, shows a handoff screen before the next
     * human's turn so the previous player's information is hidden.
     */
    private void printStatus() {
        Player current = getCurrentPlayer();

        // handoff screen between human players
        if (multipleHumans && current instanceof HumanPlayer) {
            clearScreen();
            System.out.println(">>> Pass to " + current.getName()
                    + " (" + current.getToken() + ")");
            System.out.println(">>> Press Enter when ready...");
            scanner.nextLine();
        }

        System.out.println("\n------------------------------------------------------------");
        board.printBoard(players);
        System.out.println("------------------------------------------------------------");
    }

    /**
     * Executes a single turn for the current player: movement, then suggestion
     * (if in a room), then optional accusation. Advances to the next active
     * player afterward.
     */
    public void nextTurn() {
        Player current = getCurrentPlayer();
        System.out.println("\n========== Turn " + (turnCount + 1) + ": "
                + current.getName() + " (" + current.getToken() + ") ==========");

        // 1) Movement phase
        current.takeTurn(board, dice, players);

        // 2) Suggestion phase — only if player is in a room and still active
        if (current.isActive() && current.getCurrentRoom() != null) {
            Suggestion suggestion = current.makeSuggestion();
            if (suggestion != null) {
                System.out.println(current.getName() + " suggests: " + suggestion);

                // move the accused suspect's piece to the room (rule 18)
                moveSuspectToRoom(suggestion, current.getCurrentRoom());

                // resolve suggestion — go clockwise asking each player
                resolveSuggestion(current, suggestion);

                // 3) Accusation phase — only directly after a suggestion
                if (current.isActive() && !gameOver) {
                    Accusation accusation = current.makeAccusation();
                    if (accusation != null) {
                        handleAccusation(current, accusation);
                    }
                }
            }
        }

        // advance to next active player
        turnCount++;
        if (!gameOver) {
            advanceToNextPlayer();
        }
    }

    /**
     * Resolves a suggestion by going clockwise from the suggesting player.
     * Each other player (including eliminated ones — they still hold cards)
     * checks their hand. The first player with a matching card must show
     * exactly one to the suggester.
     * @param suggester the player who made the suggestion
     * @param suggestion the suggestion to resolve
     */
    private void resolveSuggestion(Player suggester, Suggestion suggestion) {
        int startIdx = players.indexOf(suggester);

        for (int i = 1; i < players.size(); i++) {
            int idx = (startIdx + i) % players.size();
            Player other = players.get(idx);

            // even eliminated players must disprove if they can (rule 11)
            List<Card> matches = other.getMatchingCards(suggestion);
            if (!matches.isEmpty()) {
                Card shown = other.chooseSuggestionCard(matches, suggester.getName());
                System.out.println(other.getName() + " disproves the suggestion.");
                suggester.seeDisprovalCard(shown, other.getName());
                return;
            } else {
                System.out.println(other.getName() + " cannot disprove.");
            }
        }

        System.out.println("No one could disprove the suggestion!");
    }

    /**
     * Handles an accusation — checks it against the murder envelope.
     * A correct accusation wins the game. A wrong one eliminates the player.
     * @param accuser the player making the accusation
     * @param accusation the accusation to check
     */
    private void handleAccusation(Player accuser, Accusation accusation) {
        System.out.println("\n" + accuser.getName() + " accuses: " + accusation);

        if (checkAccusation(accusation)) {
            winner = accuser;
            gameOver = true;
        } else {
            System.out.println(accuser.getName() + "'s accusation was WRONG!");
            System.out.println(accuser.getName()
                    + " is eliminated but stays to disprove suggestions.");
            accuser.setActive(false);
        }
    }

    /**
     * Moves the accused suspect's playing piece to the suggestion room.
     * Per rule 18, pieces moved by a suggestion stay in their new location.
     * Matches players by checking if the suspect name contains the player's token.
     * @param suggestion the suggestion naming the suspect
     * @param room the room to move the suspect into
     */
    private void moveSuspectToRoom(Suggestion suggestion, Room room) {
        String suspectName = suggestion.getSuspect().getName();
        for (Player p : players) {
            if (suspectName.toLowerCase().contains(p.getToken().toLowerCase())) {
                if (p.getCurrentRoom() != room) {
                    if (p.getCurrentRoom() != null) {
                        p.leaveRoom();
                    }
                    p.enterRoom(room);
                    System.out.println(p.getName() + "'s piece is moved to the "
                            + room.getName() + ".");
                }
                return;
            }
        }
    }

    /**
     * Advances currentPlayerIndex to the next active player. If only one
     * (or zero) active players remain, declares a winner and ends the game.
     */
    private void advanceToNextPlayer() {
        long activeCount = 0;
        Player lastActive = null;
        for (Player p : players) {
            if (p.isActive()) {
                activeCount++;
                lastActive = p;
            }
        }

        if (activeCount <= 1) {
            winner = lastActive;
            gameOver = true;
            return;
        }

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());
    }

    /**
     * Returns true if all human players have been eliminated (wrong accusation).
     * Used to end the game early when only AI players remain.
     * @return true if no active human players remain
     */
    private boolean allHumansEliminated() {
        for (Player p : players) {
            if (p instanceof HumanPlayer && p.isActive()) {
                return false;
            }
        }
        // only counts if there was at least one human to begin with
        for (Player p : players) {
            if (p instanceof HumanPlayer) return true;
        }
        return false;
    }

    /**
     * Prints the game-over message including the winner (if any) and the
     * contents of the murder envelope.
     */
    private void printGameOver() {
        System.out.println("\n====================================");
        System.out.println("           GAME OVER");
        System.out.println("====================================");

        if (winner != null) {
            System.out.println(winner.getName() + " wins the game!");
        } else {
            System.out.println("No one solved the mystery.");
        }

        System.out.println("\nThe murder envelope contained:");
        System.out.println("  Murderer: " + murderEnvelope.getSuspect().getName());
        System.out.println("  Weapon:   " + murderEnvelope.getWeapon().getName());
        System.out.println("  Room:     " + murderEnvelope.getRoom().getName());
        System.out.println("\nThanks for playing Clue!");
    }

    /**
     * Prints blank lines to push previous content off the visible terminal.
     * Used for privacy between human players on a shared screen.
     */
    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    /**
     * Checks whether an accusation matches the murder envelope.
     * @param accusation the accusation being made
     * @return true if accusation is correct
     */
    public boolean checkAccusation(Accusation accusation) {
        return murderEnvelope.verify(
            accusation.getSuspect(),
            accusation.getWeapon(),
            accusation.getRoom()
        );
    }

    /**
     * Returns the character names for the given player index.
     * @return the full array of classic Clue! character names
     */
    public static String[] getCharacterNames() { return CHARACTER_NAMES; }

    /**
     * Returns the short token names for the given player index.
     * @return the full array of token names
     */
    public static String[] getTokenNames() { return TOKEN_NAMES; }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public List<Player> getPlayers() { return players; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public Board getBoard() { return board; }
    public Dice getDice() { return dice; }
    public MurderEnvelope getMurderEnvelope() { return murderEnvelope; }
    public int getTurnCount() { return turnCount; }
}
