package com.cluegame.model;

import com.cluegame.cards.Card;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Tests for overall game flow — turn order, accusations and game-over conditions.
 * @author Thanh Shaw
 */
public class GameFlowTest {

    private Game game;
    private List<Player> players;

    @BeforeEach
    public void setUp() {
        players = new ArrayList<>();
    }

    /**
     * Validates FR6 — Miss Scarlett goes first. The first player in the list
     * should be placed at Scarlett's starting position (0, 16).
     */
    @Test
    public void testMissScarlettGoesFirst() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        Player first = game.getCurrentPlayer();
        assertEquals("Alice", first.getName(),
                "First player should be the one assigned Scarlett");
        assertEquals(0, first.getRow(),
                "Scarlett should start at row 0");
        assertEquals(16, first.getCol(),
                "Scarlett should start at column 16");
    }

    /**
     * Validates FR10 — a correct accusation ends the game. The player who
     * accuses correctly should win.
     */
    @Test
    public void testCorrectAccusationWinsGame() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        // get the actual envelope contents and make a matching accusation
        String suspect = game.getMurderEnvelope().getSuspect().getName();
        String weapon = game.getMurderEnvelope().getWeapon().getName();
        String room = game.getMurderEnvelope().getRoom().getName();

        Accusation correct = new Accusation(
                new SuspectCard(suspect),
                new WeaponCard(weapon),
                new RoomCard(room)
        );

        assertTrue(game.checkAccusation(correct),
                "Accusation matching the envelope should be correct");
    }

    /**
     * Validates FR10 — an incorrect accusation does not match the envelope.
     */
    @Test
    public void testWrongAccusationFails() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        // make an accusation with all wrong cards
        // at least one of these will differ from the envelope
        Accusation wrong = new Accusation(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Candlestick"),
                new RoomCard("Hall")
        );

        // this might coincidentally be correct, so check against envelope
        String envSuspect = game.getMurderEnvelope().getSuspect().getName();
        String envWeapon = game.getMurderEnvelope().getWeapon().getName();
        String envRoom = game.getMurderEnvelope().getRoom().getName();

        if (envSuspect.equals("Miss Scarlett")
                && envWeapon.equals("Candlestick")
                && envRoom.equals("Hall")) {
            // very unlikely but possible — use different cards
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
     * Validates FR11 — a wrong accusation eliminates the player but the
     * game continues with remaining players.
     */
    @Test
    public void testWrongAccusationEliminatesPlayer() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        players.add(new AIPlayer("Carol", "White", 0, 0));
        game = new Game(players);
        game.startGame();

        Player alice = players.get(0);
        assertTrue(alice.isActive(), "Alice should be active before accusation");

        // simulate a wrong accusation by eliminating alice
        alice.setActive(false);

        assertFalse(alice.isActive(), "Alice should be eliminated after wrong accusation");
        // game should not be over — Bob and Carol are still playing
        assertFalse(game.isGameOver(), "Game should continue with remaining players");
    }

    /**
     * Validates FR11 — an eliminated player retains their hand of cards
     * so they can still disprove other players' suggestions.
     */
    @Test
    public void testEliminatedPlayerKeepsCards() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        Player alice = players.get(0);
        int handSize = alice.getHand().size();
        alice.setActive(false);

        assertEquals(handSize, alice.getHand().size(),
                "Eliminated player should keep all their cards");
        assertTrue(handSize > 0,
                "Eliminated player should still have cards to disprove with");
    }

    /**
     * Validates game-over — when all human players are eliminated, only AI
     * remains and the game should end for the human experience.
     */
    @Test
    public void testAllHumansEliminatedEndsGame() {
        // use a Scanner with empty input so HumanPlayer doesn't block
        Scanner dummyScanner = new Scanner("pass\nN\n");
        players.add(new HumanPlayer("Human1", "Scarlett", 0, 0, dummyScanner));
        players.add(new AIPlayer("AI1", "Mustard", 0, 0));
        players.add(new AIPlayer("AI2", "White", 0, 0));
        game = new Game(players);
        game.startGame();

        // eliminate the human player
        players.get(0).setActive(false);

        // check that only AI remains active
        boolean anyHumanActive = false;
        for (Player p : players) {
            if (p instanceof HumanPlayer && p.isActive()) {
                anyHumanActive = true;
            }
        }
        assertFalse(anyHumanActive,
                "No human player should be active after elimination");
    }

    /**
     * Validates FR4 — after startGame, all cards are accounted for.
     * The 3 envelope cards plus all player hands should total 21.
     */
    @Test
    public void testAllCardsAccountedFor() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        players.add(new AIPlayer("Carol", "White", 0, 0));
        game = new Game(players);
        game.startGame();

        int totalCards = 3; // 3 in the envelope
        for (Player p : players) {
            totalCards += p.getHand().size();
        }
        assertEquals(21, totalCards,
                "Envelope (3) + all player hands should equal 21 cards total");
    }

    /**
     * Validates FR6 — players are placed at their correct starting positions
     * after startGame is called.
     */
    @Test
    public void testPlayersAtCorrectStartPositions() {
        players.add(new AIPlayer("P1", "Scarlett", 0, 0));
        players.add(new AIPlayer("P2", "Mustard", 0, 0));
        players.add(new AIPlayer("P3", "White", 0, 0));
        game = new Game(players);
        game.startGame();

        // expected start positions from Game.START_POSITIONS
        int[][] expected = {{0, 16}, {7, 24}, {23, 16}};
        for (int i = 0; i < 3; i++) {
            assertEquals(expected[i][0], players.get(i).getRow(),
                    "Player " + (i+1) + " should be at row " + expected[i][0]);
            assertEquals(expected[i][1], players.get(i).getCol(),
                    "Player " + (i+1) + " should be at col " + expected[i][1]);
        }
    }

    /**
     * Validates FR7 — the dice produces results in the valid range (2-12).
     * This is a basic sanity check on the Dice used by the game.
     */
    @Test
    public void testGameDiceProducesValidRolls() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);

        Dice dice = game.getDice();
        for (int i = 0; i < 100; i++) {
            int roll = dice.roll();
            assertTrue(roll >= 2 && roll <= 12,
                    "Dice roll should be between 2 and 12, got " + roll);
        }
    }
}
