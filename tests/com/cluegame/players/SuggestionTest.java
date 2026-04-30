package com.cluegame.players;

import com.cluegame.cards.Card;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Accusation;
import com.cluegame.model.Suggestion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for the suggestion system — card matching and disproving logic.
 * @author Thanh Shaw
 */
public class SuggestionTest {

    /**
     * Validates FR8 — getMatchingCards returns the correct cards when a
     * player holds cards that match the suggestion.
     */
    @Test
    public void testGetMatchingCardsFindsMatches() {
        AIPlayer player = new AIPlayer("Test", "Scarlett", 0, 0);
        player.addCard(new SuspectCard("Miss Scarlett"));
        player.addCard(new WeaponCard("Rope"));
        player.addCard(new RoomCard("Kitchen"));

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Dagger"),
                new RoomCard("Kitchen")
        );

        List<Card> matches = player.getMatchingCards(suggestion);
        assertEquals(2, matches.size(),
                "Should find 2 matching cards (suspect and room)");

        // check that the right cards were found
        boolean foundScarlett = false;
        boolean foundKitchen = false;
        for (Card c : matches) {
            if (c.getName().equals("Miss Scarlett")) foundScarlett = true;
            if (c.getName().equals("Kitchen")) foundKitchen = true;
        }
        assertTrue(foundScarlett, "Should find Miss Scarlett as a match");
        assertTrue(foundKitchen, "Should find Kitchen as a match");
    }

    /**
     * Validates FR8 — getMatchingCards returns an empty list when the
     * player holds none of the cards named in the suggestion.
     */
    @Test
    public void testGetMatchingCardsReturnsEmptyWhenNoMatch() {
        AIPlayer player = new AIPlayer("Test", "Scarlett", 0, 0);
        player.addCard(new SuspectCard("Mrs White"));
        player.addCard(new WeaponCard("Rope"));
        player.addCard(new RoomCard("Hall"));

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Colonel Mustard"),
                new WeaponCard("Dagger"),
                new RoomCard("Kitchen")
        );

        List<Card> matches = player.getMatchingCards(suggestion);
        assertTrue(matches.isEmpty(),
                "Should return empty list when no cards match");
    }

    /**
     * Validates FR8 — when no player can disprove a suggestion, the
     * suggesting player is not shown any card. Tested by checking that
     * all players return empty matching card lists.
     */
    @Test
    public void testNoPlayerCanDisprove() {
        // set up players whose hands don't match the suggestion at all
        AIPlayer suggester = new AIPlayer("Suggester", "Scarlett", 0, 0);
        AIPlayer other1 = new AIPlayer("Other1", "Mustard", 0, 0);
        AIPlayer other2 = new AIPlayer("Other2", "White", 0, 0);

        other1.addCard(new SuspectCard("Mrs White"));
        other1.addCard(new WeaponCard("Rope"));
        other2.addCard(new RoomCard("Hall"));
        other2.addCard(new SuspectCard("Mrs Peacock"));

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Colonel Mustard"),
                new WeaponCard("Dagger"),
                new RoomCard("Kitchen")
        );

        // neither other player should be able to disprove
        assertTrue(other1.getMatchingCards(suggestion).isEmpty(),
                "Other1 should not be able to disprove");
        assertTrue(other2.getMatchingCards(suggestion).isEmpty(),
                "Other2 should not be able to disprove");
    }

    /**
     * Validates FR8 — getMatchingCards correctly matches all three card
     * types (suspect, weapon and room) when the player holds all of them.
     */
    @Test
    public void testGetMatchingCardsMatchesAllThreeTypes() {
        AIPlayer player = new AIPlayer("Test", "Scarlett", 0, 0);
        player.addCard(new SuspectCard("Colonel Mustard"));
        player.addCard(new WeaponCard("Dagger"));
        player.addCard(new RoomCard("Kitchen"));

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Colonel Mustard"),
                new WeaponCard("Dagger"),
                new RoomCard("Kitchen")
        );

        List<Card> matches = player.getMatchingCards(suggestion);
        assertEquals(3, matches.size(),
                "Should match all three cards when player holds them all");
    }

    /**
     * Validates FR8 — AI player's chooseSuggestionCard returns the first
     * matching card (AI shows cards automatically).
     */
    @Test
    public void testAIChoosesFirstMatchingCard() {
        AIPlayer player = new AIPlayer("Test", "Scarlett", 0, 0);
        Card scarlett = new SuspectCard("Miss Scarlett");
        Card kitchen = new RoomCard("Kitchen");
        player.addCard(scarlett);
        player.addCard(kitchen);

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Dagger"),
                new RoomCard("Kitchen")
        );

        List<Card> matches = player.getMatchingCards(suggestion);
        Card shown = player.chooseSuggestionCard(matches, "Asker");

        // AI should show the first match found
        assertEquals(matches.get(0), shown,
                "AI should show the first matching card");
    }

    /**
     * Validates FR9 — AI records shown cards in its detective notepad
     * for future deduction.
     */
    @Test
    public void testAIRecordsDisprovalInNotepad() {
        AIPlayer ai = new AIPlayer("AI", "Scarlett", 0, 0);
        Card shown = new WeaponCard("Dagger");

        assertFalse(ai.getNotepad().getSeenCards().contains("Dagger"),
                "Notepad should not contain Dagger before seeing it");

        ai.seeDisprovalCard(shown, "Other");

        assertTrue(ai.getNotepad().getSeenCards().contains("Dagger"),
                "Notepad should contain Dagger after seeing it");
    }

    /**
     * Validates FR9 — AI marks its own dealt cards in the notepad.
     * Cards in the AI's hand cannot be in the murder envelope.
     */
    @Test
    public void testAIMarksOwnCardsInNotepad() {
        AIPlayer ai = new AIPlayer("AI", "Scarlett", 0, 0);
        ai.addCard(new SuspectCard("Mrs White"));
        ai.addCard(new WeaponCard("Rope"));

        assertTrue(ai.getNotepad().getSeenCards().contains("Mrs White"),
                "AI should mark its own suspect card as seen");
        assertTrue(ai.getNotepad().getSeenCards().contains("Rope"),
                "AI should mark its own weapon card as seen");
    }

    /**
     * Validates FR17 — a suggestion must use the room the player is in.
     * AI's makeSuggestion returns null when not in a room.
     */
    @Test
    public void testSuggestionRequiresRoom() {
        AIPlayer ai = new AIPlayer("AI", "Scarlett", 0, 0);
        assertNull(ai.makeSuggestion(),
                "AI should not suggest when not in a room");
    }

    /**
     * Validates FR17 — when the AI is in a room, the suggestion uses that room.
     */
    @Test
    public void testSuggestionUsesCurrentRoom() {
        AIPlayer ai = new AIPlayer("AI", "Scarlett", 0, 0);
        ai.enterRoom(new com.cluegame.model.Room("Kitchen"));

        Suggestion suggestion = ai.makeSuggestion();
        assertNotNull(suggestion, "AI should make a suggestion when in a room");
        assertEquals("Kitchen", suggestion.getRoom().getName(),
                "Suggestion room should match the AI's current room");
    }

    /**
     * Validates FR9 — an undisproved suggestion can partially confirm
     * envelope cards even when the AI holds one card from the suggestion.
     */
    @Test
    public void testAIUsesPartialUndisprovedSuggestionToAccuseLater() {
        AIPlayer ai = new AIPlayer("AI", "Scarlett", 0, 0);
        ai.addCard(new RoomCard("Ballroom"));

        ai.setUndisprovedSuggestion(new Suggestion(
                new SuspectCard("Reverend Green"),
                new WeaponCard("Rope"),
                new RoomCard("Ballroom")
        ));

        assertNull(ai.makeAccusation(),
                "AI should not accuse while the room is still unconfirmed");

        ai.enterRoom(new com.cluegame.model.Room("Billiard Room"));
        Suggestion followUp = ai.makeSuggestion();

        assertEquals("Reverend Green", followUp.getSuspect().getName(),
                "AI should reuse the confirmed suspect");
        assertEquals("Rope", followUp.getWeapon().getName(),
                "AI should reuse the confirmed weapon");
        assertEquals("Billiard Room", followUp.getRoom().getName(),
                "AI should test the current room");

        ai.setUndisprovedSuggestion(followUp);
        Accusation accusation = ai.makeAccusation();

        assertNotNull(accusation,
                "AI should accuse once suspect, weapon and room are confirmed");
        assertEquals("Reverend Green", accusation.getSuspect().getName());
        assertEquals("Rope", accusation.getWeapon().getName());
        assertEquals("Billiard Room", accusation.getRoom().getName());
    }
}
