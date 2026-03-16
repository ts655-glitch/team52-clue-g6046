package com.cluegame.players;

import com.cluegame.model.Board;
import com.cluegame.model.Suggestion;

/**
 * Represents a human-controlled player in Clue!
 * Decisions are made via the GUI in later sprints.
 * @author Team 52
 */
public class HumanPlayer extends Player {

    /**
     * Constructs a human player with a name, token and starting position.
     * @param name the player's name
     * @param token the player's token identifier
     * @param startRow starting row on the board
     * @param startCol starting column on the board
     */
    public HumanPlayer(String name, String token, int startRow, int startCol) {
        super(name, token, startRow, startCol);
    }

    /**
     * Takes a turn — in Sprint 1 this is a placeholder.
     * Will be driven by GUI input in Sprint 2.
     * @param board the game board
     */
    @Override
    public void takeTurn(Board board) {
        // GUI-driven in Sprint 2
        System.out.println(getName() + " is taking their turn.");
    }

    /**
     * Makes a suggestion — placeholder for Sprint 1.
     * Will be driven by GUI input in Sprint 2.
     * @return null for now
     */
    @Override
    public Suggestion makeSuggestion() {
        // GUI-driven in Sprint 2
        return null;
    }
}