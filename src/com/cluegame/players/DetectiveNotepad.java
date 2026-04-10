package com.cluegame.players;

import com.cluegame.cards.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks which cards a player has seen or eliminated during the game.
 * Used by AI to make informed decisions about when to accuse.
 * @author Thanh Shaw
 */
public class DetectiveNotepad {

    private Set<String> seenCards;

    public DetectiveNotepad() {
        seenCards = new HashSet<>();
    }

    /**
     * Marks a card as seen/eliminated from suspicion.
     * @param card the card to mark as known
     */
    public void markSeen(Card card) {
        seenCards.add(card.getName());
    }

    /**
     * Marks a card name as seen/eliminated from suspicion.
     * @param cardName the name of the card to mark
     */
    public void markSeen(String cardName) {
        seenCards.add(cardName);
    }

    /**
     * Returns true if this card has already been seen.
     * @param card the card to check
     * @return true if card is known
     */
    public boolean isSeen(Card card) {
        return seenCards.contains(card.getName());
    }

    /**
     * Given a complete list of names in a category (e.g. all 6 suspects),
     * returns the single unseen name if exactly one remains. This means
     * the AI has deduced that card must be in the murder envelope.
     * Returns null if more than one name is still unseen.
     * @param allNames all possible names in the category
     * @return the deduced name, or null if not yet deducible
     */
    public String deduceMissing(String[] allNames) {
        List<String> unseen = new ArrayList<>();
        for (String name : allNames) {
            if (!seenCards.contains(name)) {
                unseen.add(name);
            }
        }
        return unseen.size() == 1 ? unseen.get(0) : null;
    }

    public Set<String> getSeenCards() { return seenCards; }
}
