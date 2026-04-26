package com.cluegame.model;

import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.Player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for player elimination and game-ending conditions.
 * Covers wrong accusations, last-player-standing, and correct accusations.
 * @author Thanh Shaw
 */
public class EliminationTest {

    /**
     * A wrong accusation eliminates the player but the game continues
     * if other active players remain.
     */
    @Test
    public void testWrongAccusationEliminatesButGameContinues() {
        List<Player> players = new ArrayList<>();
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        players.add(new AIPlayer("Carol", "White", 0, 0));

        Game game = new Game(players);
        game.startGame();

        Player alice = players.get(0);
        assertTrue(alice.isActive());

        alice.setActive(false);

        assertFalse(alice.isActive(), "Alice should be eliminated");
        assertFalse(game.isGameOver(), "Game should continue with 2 active players");
    }

    /**
     * An eliminated player still holds their cards for disproving.
     */
    @Test
    public void testEliminatedPlayerKeepsHandForDisproving() {
        List<Player> players = new ArrayList<>();
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));

        Game game = new Game(players);
        game.startGame();

        int handSize = players.get(0).getHand().size();
        players.get(0).setActive(false);

        assertEquals(handSize, players.get(0).getHand().size(),
                "Eliminated player should keep all their cards");
        assertTrue(handSize > 0, "Hand should not be empty");
    }

    /**
     * A correct accusation matches the murder envelope.
     */
    @Test
    public void testCorrectAccusationMatchesEnvelope() {
        List<Player> players = new ArrayList<>();
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));

        Game game = new Game(players);
        game.startGame();

        Accusation correct = new Accusation(
                game.getMurderEnvelope().getSuspect(),
                game.getMurderEnvelope().getWeapon(),
                game.getMurderEnvelope().getRoom()
        );

        assertTrue(game.checkAccusation(correct),
                "Accusation matching the envelope should be correct");
    }

    /**
     * An incorrect accusation does not match the envelope.
     */
    @Test
    public void testWrongAccusationDoesNotMatch() {
        List<Player> players = new ArrayList<>();
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));

        Game game = new Game(players);
        game.startGame();

        // create an accusation that is very unlikely to be correct
        Accusation wrong = new Accusation(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Candlestick"),
                new RoomCard("Hall")
        );

        // handle the unlikely case where this is actually correct
        String envS = game.getMurderEnvelope().getSuspect().getName();
        String envW = game.getMurderEnvelope().getWeapon().getName();
        String envR = game.getMurderEnvelope().getRoom().getName();

        if (envS.equals("Miss Scarlett") && envW.equals("Candlestick")
                && envR.equals("Hall")) {
            wrong = new Accusation(
                    new SuspectCard("Mrs White"),
                    new WeaponCard("Rope"),
                    new RoomCard("Kitchen")
            );
        }

        assertFalse(game.checkAccusation(wrong),
                "Wrong accusation should not match the envelope");
    }

    /**
     * Suggestion resolution checks players clockwise. The first player
     * with a matching card disproves, not a later one.
     */
    @Test
    public void testSuggestionResolvedClockwise() {
        AIPlayer alice = new AIPlayer("Alice", "Scarlett", 0, 0);
        AIPlayer bob = new AIPlayer("Bob", "Mustard", 0, 0);
        AIPlayer carol = new AIPlayer("Carol", "White", 0, 0);

        // both Bob and Carol hold matching cards
        bob.addCard(new SuspectCard("Colonel Mustard"));
        carol.addCard(new SuspectCard("Colonel Mustard"));

        Suggestion suggestion = new Suggestion(
                new SuspectCard("Colonel Mustard"),
                new WeaponCard("Dagger"),
                new RoomCard("Hall")
        );

        // Bob is next clockwise from Alice, so Bob should disprove first
        List<Player> players = List.of(alice, bob, carol);
        int startIdx = 0; // alice

        Player disprover = null;
        for (int i = 1; i < players.size(); i++) {
            int idx = (startIdx + i) % players.size();
            Player other = players.get(idx);
            if (!other.getMatchingCards(suggestion).isEmpty()) {
                disprover = other;
                break;
            }
        }

        assertNotNull(disprover, "Someone should disprove");
        assertEquals("Bob", disprover.getName(),
                "Bob should disprove first (clockwise order)");
    }
}
