package com.cluegame.players;
import com.cluegame.model.Suggestion;

/**
 * A human player whose decisions are driven by GUI input.
 * @author Team 52
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name, String tokenColour) {
        super(name, tokenColour);
    }

    /** Human turn is event-driven from the GUI. */
    @Override
    public void takeTurn() {
        // GUI handles this via button clicks
    }

    /** Human suggestion is built from GUI selections. */
    @Override
    public Suggestion makeSuggestion() {
        // TODO: return suggestion constructed from GUI input
        return null;
    }
}
