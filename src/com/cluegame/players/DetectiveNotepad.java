package com.cluegame.players;
import com.cluegame.cards.Card;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks which cards a player has seen or eliminated during the game.
 * Used by AI to make informed decisions.
 * @author Thanh Shaw
 */
public class DetectiveNotepad {
    private Set<String> seenCards;

    public DetectiveNotepad() { seenCards = new HashSet<>(); }

    /**
     * Marks a card as seen/eliminated from suspicion.
     * @param card the card to mark as known
     */
    public void markSeen(Card card) { seenCards.add(card.getName()); }

    /**
     * Returns true if this card has already been seen.
     * @param card the card to check
     * @return true if card is known
     */
    public boolean isSeen(Card card) { return seenCards.contains(card.getName()); }

    public Set<String> getSeenCards() { return seenCards; }
}