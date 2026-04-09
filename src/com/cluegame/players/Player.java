package com.cluegame.players;

import com.cluegame.cards.Card;
import com.cluegame.model.Board;
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
    }

    /**
     * Takes a turn — implemented differently by Human and AI players.
     * @param board the game board
     */
    public abstract void takeTurn(Board board);

    /**
     * Makes a suggestion — implemented differently by Human and AI players.
     * @return a Suggestion object
     */
    public abstract Suggestion makeSuggestion();

    /**
     * Adds a card to this player's hand.
     * @param card the card to add
     */
    public void addCard(Card card) {
        hand.add(card);
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

    public String getName() { return name; }
    public String getToken() { return token; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public List<Card> getHand() { return hand; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}