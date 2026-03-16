package com.cluegame.players;

import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Board;
import com.cluegame.model.Suggestion;

import java.util.Random;

/**
 * Represents an autonomous AI player in Clue!
 * Makes random decisions as permitted by the spec.
 * @author Team 52
 */
public class AIPlayer extends Player {

    private Random random;

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
    }

    /**
     * Takes a turn — AI moves randomly to an adjacent square.
     * @param board the game board
     */
    @Override
    public void takeTurn(Board board) {
        System.out.println(getName() + " (AI) is taking their turn.");
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};
        int direction = random.nextInt(4);
        int newRow = getRow() + dRow[direction];
        int newCol = getCol() + dCol[direction];
        if (newRow >= 0 && newRow < Board.ROWS &&
                newCol >= 0 && newCol < Board.COLS) {
            moveTo(newRow, newCol);
            System.out.println(getName() + " moved to (" + newRow + ", " + newCol + ")");
        }
    }

    /**
     * Makes a random suggestion.
     * @return a randomly constructed Suggestion
     */
    @Override
    public Suggestion makeSuggestion() {
        String[] suspects = {"Miss Scarlett", "Colonel Mustard", "Mrs White",
                "Reverend Green", "Mrs Peacock", "Professor Plum"};
        String[] weapons = {"Candlestick", "Knife", "Lead Pipe",
                "Revolver", "Rope", "Wrench"};
        String[] rooms = {"Kitchen", "Ballroom", "Conservatory",
                "Billiard Room", "Library", "Study",
                "Hall", "Lounge", "Dining Room"};
        return new Suggestion(
                new SuspectCard(suspects[random.nextInt(suspects.length)]),
                new WeaponCard(weapons[random.nextInt(weapons.length)]),
                new RoomCard(rooms[random.nextInt(rooms.length)])
        );
    }
}