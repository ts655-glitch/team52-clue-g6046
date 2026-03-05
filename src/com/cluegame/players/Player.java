package com.cluegame.players;
import com.cluegame.cards.Card;
import com.cluegame.model.Suggestion;
import com.cluegame.model.Square;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract base class for all players (human and AI).
 * @author Team 52
 */
public abstract class Player {
    private String name;
    private String tokenColour;
    private List<Card> hand;
    private Square position;
    private boolean eliminated;
    private DetectiveNotepad notepad;

    public Player(String name, String tokenColour) {
        this.name = name;
        this.tokenColour = tokenColour;
        this.hand = new ArrayList<>();
        this.eliminated = false;
        this.notepad = new DetectiveNotepad();
    }

    /** Executes this player's turn. Implemented differently for human vs AI. */
    public abstract void takeTurn();

    /**
     * Decides on a suggestion. Human uses GUI; AI uses logic.
     * @return the suggestion to make
     */
    public abstract Suggestion makeSuggestion();

    /**
     * Shows a card from hand that matches the suggestion, if held.
     * @param suggestion the suggestion to respond to
     * @return a matching card, or null if none held
     */
    public Card showCard(Suggestion suggestion) {
        for (Card card : hand) {
            if (card.getName().equals(suggestion.getSuspect().getName()) ||
                    card.getName().equals(suggestion.getWeapon().getName()) ||
                    card.getName().equals(suggestion.getRoom().getName())) {
                return card;
            }
        }
        return null;
    }

    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public void addCard(Card card) { hand.add(card); }
    public Square getPosition() { return position; }
    public void setPosition(Square position) { this.position = position; }
    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }
    public DetectiveNotepad getNotepad() { return notepad; }
}
