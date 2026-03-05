package com.cluegame.model;

import com.cluegame.players.Player;
import com.cluegame.cards.Deck;
import com.cluegame.cards.MurderEnvelope;
import java.util.List;
import java.util.ArrayList;

/**
 * Top-level game controller. Manages turn order, game state and win condition.
 * @author Team 52
 */
public class Game {

    private Board board;
    private Deck deck;
    private MurderEnvelope murderEnvelope;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;

    /**
     * Constructs a new Game with the given list of players.
     * @param players list of players (human and/or AI)
     */
    public Game(List<Player> players) {
        this.players = players;
        this.board = new Board();
        this.murderEnvelope = new MurderEnvelope();
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    /**
     * Initialises and starts the game. Deals cards and places tokens.
     */
    public void startGame() {
        // TODO: shuffle and deal cards
        // TODO: place tokens on starting squares
        // TODO: populate murder envelope
    }

    /**
     * Advances the game to the next player's turn.
     */
    public void nextTurn() {
        // TODO: implement turn progression
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
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

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public boolean isGameOver() { return gameOver; }
    public Board getBoard() { return board; }
}
