package com.cluegame.players;

import com.cluegame.cards.Card;
import com.cluegame.model.Accusation;
import com.cluegame.model.Board;
import com.cluegame.model.Dice;
import com.cluegame.model.Room;
import com.cluegame.model.Suggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a player in Clue!
 * Can be extended by HumanPlayer or AIPlayer.
 * @author Thanh Shaw
 */
public abstract class Player {

    private String name;
    private String token; // colour or symbol representing this player
    private int row;
    private int col;
    private List<Card> hand;
    private boolean active; // false if player has made a wrong accusation
    private Room currentRoom; // the room the player is in, or null if in a corridor

    /**
     * Constructs a player with a name, token and starting position.
     * @param name the player's name
     * @param token the player's token identifier
     * @param startRow starting row on the board
     * @param startCol starting column on the board
     */
    public Player(String name, String token, int startRow, int startCol) {
        this.name = name;
        this.token = token;
        this.row = startRow;
        this.col = startCol;
        this.hand = new ArrayList<>();
        this.active = true;
        this.currentRoom = null;
    }

    /**
     * Takes a turn — handles movement for this player. Implemented differently
     * by Human (console input) and AI (random/strategic) players.
     * @param board the game board
     * @param dice the dice to roll for movement
     * @param allPlayers all players in the game (for corridor occupancy checks)
     */
    public abstract void takeTurn(Board board, Dice dice, List<Player> allPlayers);

    /**
     * Makes a suggestion. The room must be the room the player is currently in.
     * Returns null if the player chooses not to suggest or is not in a room.
     * @return a Suggestion, or null if none made
     */
    public abstract Suggestion makeSuggestion();

    /**
     * Makes an accusation. Returns null if the player chooses not to accuse.
     * A wrong accusation eliminates the player from active play.
     * @return an Accusation, or null if none made
     */
    public abstract Accusation makeAccusation();

    /**
     * Called when this player must disprove a suggestion and holds one or more
     * matching cards. The player chooses which card to reveal to the suggester.
     * @param matchingCards the cards in this player's hand that match the suggestion
     * @param askerName the name of the player who made the suggestion
     * @return the card chosen to reveal
     */
    public abstract Card chooseSuggestionCard(List<Card> matchingCards, String askerName);

    /**
     * Called when another player shows this player a card to disprove a suggestion.
     * Human players see the card printed; AI players record it in their notepad.
     * @param card the card being shown
     * @param fromPlayerName the name of the player showing the card
     */
    public abstract void seeDisprovalCard(Card card, String fromPlayerName);

    /**
     * Adds a card to this player's hand.
     * @param card the card to add
     */
    public void addCard(Card card) {
        hand.add(card);
    }

    /**
     * Returns all cards in this player's hand that match a suggestion.
     * A card matches if its name equals the suspect, weapon or room name.
     * @param suggestion the suggestion to check against
     * @return list of matching cards (may be empty)
     */
    public List<Card> getMatchingCards(Suggestion suggestion) {
        List<Card> matches = new ArrayList<>();
        for (Card card : hand) {
            if (card.getName().equals(suggestion.getSuspect().getName())
                    || card.getName().equals(suggestion.getWeapon().getName())
                    || card.getName().equals(suggestion.getRoom().getName())) {
                matches.add(card);
            }
        }
        return matches;
    }

    /**
     * Moves the player to a new position on the board.
     * @param row the new row
     * @param col the new column
     */
    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Places the player inside a room. The player's grid position stays
     * at the door they entered through but they are considered inside the room.
     * @param room the room to enter
     */
    public void enterRoom(Room room) {
        this.currentRoom = room;
        room.addOccupant(name);
    }

    /**
     * Removes the player from their current room so they can move
     * through corridors again.
     */
    public void leaveRoom() {
        if (currentRoom != null) {
            currentRoom.removeOccupant(name);
            currentRoom = null;
        }
    }

    public String getName() { return name; }
    public String getToken() { return token; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public List<Card> getHand() { return hand; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Room getCurrentRoom() { return currentRoom; }
}
