package com.cluegame.players;
import com.cluegame.model.Suggestion;
import java.util.Random;

/**
 * An autonomous AI player using random decision making.
 * Can be upgraded with smarter logic in a later sprint.
 * @author Team 52
 */
public class AIPlayer extends Player {
    private Random random;

    public AIPlayer(String name, String tokenColour) {
        super(name, tokenColour);
        this.random = new Random();
    }

    /** AI takes a turn using random movement and suggestion logic. */
    @Override
    public void takeTurn() {
        // TODO: random move, then random suggestion
    }

    /** AI picks a random suggestion from cards not yet eliminated on its notepad. */
    @Override
    public Suggestion makeSuggestion() {
        // TODO: pick randomly from unknown cards on notepad
        return null;
    }
}