package com.cluegame.model;

import com.cluegame.cards.Card;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the Game class — specifically the startGame() setup logic.
 * @author Thanh Shaw
 */
public class GameTest {

    private Game game;
    private List<Player> players;

    @BeforeEach
    public void setUp() {
        players = new ArrayList<>();
        players.add(new HumanPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();
    }

    /**
     * Checks the murder envelope is sealed after startGame() runs.
     * Covers requirement: envelope must be populated before play begins.
     */
    @Test
    public void testEnvelopeIsSealedAfterStart() {
        assertTrue(game.getBoard() != null, "Board should be initialised");
        // access envelope via checkAccusation to confirm it was sealed
        // if envelope isn't sealed, verify() would throw a NullPointerException
        assertDoesNotThrow(() -> {
            game.checkAccusation(new Accusation(
                game.getMurderEnvelope().getSuspect(),
                game.getMurderEnvelope().getWeapon(),
                game.getMurderEnvelope().getRoom()
            ));
        });
    }

    /**
     * Checks that each player receives 9 cards when 2 players are in the game.
     * Covers requirement: 18 remaining cards dealt evenly to 2 players = 9 each.
     */
    @Test
    public void testEachPlayerHasNineCards() {
        for (Player player : players) {
            assertEquals(9, player.getHand().size(),
                player.getName() + " should have 9 cards but has " + player.getHand().size());
        }
    }

    /**
     * Checks that no card in any player's hand is also in the murder envelope.
     * Covers requirement: envelope cards must be removed from the deck before dealing.
     */
    @Test
    public void testNoHandCardMatchesEnvelope() {
        String envelopeSuspect = game.getMurderEnvelope().getSuspect().getName();
        String envelopeWeapon = game.getMurderEnvelope().getWeapon().getName();
        String envelopeRoom = game.getMurderEnvelope().getRoom().getName();

        for (Player player : players) {
            for (Card card : player.getHand()) {
                assertFalse(card.getName().equals(envelopeSuspect),
                    "Envelope suspect found in " + player.getName() + "'s hand");
                assertFalse(card.getName().equals(envelopeWeapon),
                    "Envelope weapon found in " + player.getName() + "'s hand");
                assertFalse(card.getName().equals(envelopeRoom),
                    "Envelope room found in " + player.getName() + "'s hand");
            }
        }
    }
}
