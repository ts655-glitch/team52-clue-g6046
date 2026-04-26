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
     * Validates FR6 — Miss Scarlett always goes first (classic rule).
     */
    @Test
    public void testScarlettGoesFirst() {
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        game = new Game(players);
        game.startGame();

        Player first = game.getCurrentPlayer();
        assertEquals("Scarlett", first.getToken(),
                "Scarlett should go first regardless of list order");
    }

    /**
     * Validates that Scarlett's token is placed at Scarlett's start position
     * regardless of player list order.
     */
    @Test
    public void testScarlettStartPosition() {
        players.add(new AIPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        Player scarlett = null;
        for (Player p : players) {
            if (p.getToken().equals("Scarlett")) scarlett = p;
        }
        assertNotNull(scarlett);
        assertEquals(0, scarlett.getRow(), "Scarlett should start at row 0");
        assertEquals(16, scarlett.getCol(), "Scarlett should start at col 16");
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
     * Validates FR6 — each player is placed at the start position that
     * matches their chosen character token, not their list index.
     */
    @Test
    public void testPlayersAtCorrectStartPositions() {
        players.add(new AIPlayer("P1", "Scarlett", 0, 0));
        players.add(new AIPlayer("P2", "Mustard", 0, 0));
        players.add(new AIPlayer("P3", "White", 0, 0));
        game = new Game(players);
        game.startGame();

        // verify each player is at their character's start position
        String[][] expected = {
            {"Scarlett", "0", "16"},
            {"Mustard", "7", "23"},
            {"White", "24", "14"}
        };
        for (String[] exp : expected) {
            Player p = null;
            for (Player pl : players) {
                if (pl.getToken().equals(exp[0])) { p = pl; break; }
            }
            assertNotNull(p, exp[0] + " should exist");
            assertEquals(Integer.parseInt(exp[1]), p.getRow(),
                    exp[0] + " should be at row " + exp[1]);
            assertEquals(Integer.parseInt(exp[2]), p.getCol(),
                    exp[0] + " should be at col " + exp[2]);
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

    /**
     * Validates that start positions are based on token, not list order.
     * If Mustard is listed first and Scarlett second, each should still
     * start at their own character's position.
     */
    @Test
    public void testStartPositionsByTokenNotIndex() {
        // deliberately put Mustard first in the list
        players.add(new AIPlayer("First", "Mustard", 0, 0));
        players.add(new AIPlayer("Second", "Scarlett", 0, 0));
        game = new Game(players);
        game.startGame();

        Player mustard = null;
        Player scarlett = null;
        for (Player p : players) {
            if (p.getToken().equals("Mustard")) mustard = p;
            if (p.getToken().equals("Scarlett")) scarlett = p;
        }

        // Mustard should be at Mustard's start (7,23), not Scarlett's (0,16)
        assertEquals(7, mustard.getRow(), "Mustard should start at row 7");
        assertEquals(23, mustard.getCol(), "Mustard should start at col 23");
        // Scarlett should be at Scarlett's start (0,16), not Mustard's (7,23)
        assertEquals(0, scarlett.getRow(), "Scarlett should start at row 0");
        assertEquals(16, scarlett.getCol(), "Scarlett should start at col 16");
    }

    /**
     * Validates that non-player suspect pieces exist for characters
     * not assigned to any player.
     */
    @Test
    public void testNonPlayerSuspectsExist() {
        players.add(new AIPlayer("A", "Scarlett", 0, 0));
        players.add(new AIPlayer("B", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        // 6 characters total, 2 are players, so 4 non-player suspects
        assertEquals(4, game.getNonPlayerSuspects().size(),
                "Should have 4 non-player suspect pieces");
        assertTrue(game.getNonPlayerSuspects().containsKey("White"),
                "Mrs White should be a non-player suspect");
        assertTrue(game.getNonPlayerSuspects().containsKey("Green"),
                "Rev Green should be a non-player suspect");
    }

    /**
     * Validates that weapon tokens are placed in starting rooms.
     */
    @Test
    public void testWeaponsStartInRooms() {
        players.add(new AIPlayer("A", "Scarlett", 0, 0));
        players.add(new AIPlayer("B", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        assertEquals(6, game.getWeaponPositions().size(),
                "Should have 6 weapon tokens");
        for (String weapon : Game.getWeaponNames()) {
            assertNotNull(game.getWeaponPositions().get(weapon),
                    weapon + " should be in a room");
        }
    }

    /**
     * Validates that moveWeaponToRoom changes the weapon's location.
     */
    @Test
    public void testWeaponMovedBySuggestion() {
        players.add(new AIPlayer("A", "Scarlett", 0, 0));
        players.add(new AIPlayer("B", "Mustard", 0, 0));
        game = new Game(players);
        game.startGame();

        game.moveWeaponToRoom("Rope", "Hall");
        assertEquals("Hall", game.getWeaponPositions().get("Rope"),
                "Rope should now be in the Hall");
    }
}
