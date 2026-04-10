package com.cluegame.cards;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for the Deck class.
 * @author Thanh Shaw
 */
public class DeckTest {

    /**
     * Checks a freshly created deck has exactly 21 cards (6 suspects + 6 weapons + 9 rooms).
     * Covers requirement: full Clue deck contains 21 cards.
     */
    @Test
    public void testDeckHas21Cards() {
        Deck deck = new Deck();
        assertEquals(21, deck.size(), "Deck should contain 21 cards");
    }

    /**
     * Counts each card type in a new deck and verifies 6 suspects, 6 weapons and 9 rooms.
     * Covers requirement: correct card distribution across all three types.
     */
    @Test
    public void testDeckCardTypeDistribution() {
        Deck deck = new Deck();
        List<Card> cards = deck.getCards();

        int suspects = 0;
        int weapons = 0;
        int rooms = 0;

        for (Card card : cards) {
            if (card instanceof SuspectCard) suspects++;
            else if (card instanceof WeaponCard) weapons++;
            else if (card instanceof RoomCard) rooms++;
        }

        assertEquals(6, suspects, "Should be 6 suspect cards");
        assertEquals(6, weapons, "Should be 6 weapon cards");
        assertEquals(9, rooms, "Should be 9 room cards");
    }
}

