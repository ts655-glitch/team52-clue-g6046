package com.cluegame.gui;

import com.cluegame.cards.Card;
import com.cluegame.cards.MurderEnvelope;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.model.Accusation;
import com.cluegame.model.Board;
import com.cluegame.model.Dice;
import com.cluegame.model.Game;
import com.cluegame.model.Room;
import com.cluegame.model.Suggestion;
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
 * Unit tests for the GUI GameController. These test movement validation,
 * room exit options, suggestion resolution, and accusation logic
 * without requiring the JavaFX runtime.
 * @author Thanh Shaw
 */
public class GameControllerTest {

    private GameController controller;
    private Board board;
    private List<Player> players;

    @BeforeEach
    public void setUp() {
        players = new ArrayList<>();
        players.add(new HumanPlayer("Alice", "Scarlett", 0, 0,
                new Scanner(System.in)));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));

        Game game = new Game(players);
        game.startGame();

        board = game.getBoard();
        controller = new GameController(game);
    }

    /**
     * Rolling dice starts the movement phase with steps remaining.
     */
    @Test
    public void testRollDiceStartsMovement() {
        int roll = controller.rollDice();

        assertTrue(roll >= 2 && roll <= 12, "Roll should be 2-12");
        assertTrue(controller.isMovementActive(), "Movement should be active");
        assertEquals(roll, controller.getRemainingSteps());
    }

    /**
     * Moving to a valid square decrements remaining steps.
     */
    @Test
    public void testValidMoveDecrementsSteps() {
        controller.rollDice();
        int stepsBefore = controller.getRemainingSteps();

        // Scarlett starts at (0,16). (1,16) should be a valid corridor move.
        GameController.MoveResult result = controller.tryMove(1, 16);

        assertEquals(GameController.MoveResult.MOVED, result);
        assertEquals(stepsBefore - 1, controller.getRemainingSteps());
    }

    /**
     * Moving to an invalid square returns INVALID and does not change steps.
     */
    @Test
    public void testInvalidMoveIsRejected() {
        controller.rollDice();
        int stepsBefore = controller.getRemainingSteps();

        // (5,5) is far from Scarlett's position — should be invalid
        GameController.MoveResult result = controller.tryMove(5, 5);

        assertEquals(GameController.MoveResult.INVALID, result);
        assertEquals(stepsBefore, controller.getRemainingSteps());
    }

    /**
     * endMove() stops movement immediately.
     */
    @Test
    public void testEndMoveStopsMovement() {
        controller.rollDice();
        controller.endMove();

        assertFalse(controller.isMovementActive());
        assertEquals(0, controller.getRemainingSteps());
    }

    /**
     * Room exit options include doors and secret passages.
     */
    @Test
    public void testRoomExitOptionsIncludeDoorsAndPassages() {
        Player alice = controller.getCurrentPlayer();
        Room study = board.getRoom("Study");
        alice.enterRoom(study);

        List<String> options = controller.getRoomExitOptions();

        // Study has 1 door + 1 secret passage (to Kitchen)
        assertTrue(options.size() >= 2,
                "Study should have at least a door and a secret passage");
        boolean hasPassage = false;
        for (String opt : options) {
            if (opt.contains("Secret passage")) hasPassage = true;
        }
        assertTrue(hasPassage, "Study exit options should include secret passage");
    }

    /**
     * Exiting a room via secret passage moves the player to the destination room.
     */
    @Test
    public void testExitRoomViaSecretPassage() {
        Player alice = controller.getCurrentPlayer();
        Room study = board.getRoom("Study");
        alice.enterRoom(study);

        List<String> options = controller.getRoomExitOptions();
        int passageIndex = -1;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).contains("Secret passage")) {
                passageIndex = i;
                break;
            }
        }
        assertTrue(passageIndex >= 0, "Should find secret passage option");

        controller.exitRoom(passageIndex);

        assertEquals("Kitchen", alice.getCurrentRoom().getName(),
                "Alice should be in Kitchen after using secret passage");
    }

    /**
     * Suggestion resolution finds a matching card from another player.
     */
    @Test
    public void testSuggestionResolution() {
        Player alice = controller.getCurrentPlayer();
        Room hall = board.getRoom("Hall");
        alice.enterRoom(hall);

        // find a card that Bob holds
        Player bob = players.get(1);
        if (bob.getHand().isEmpty()) return; // edge case — skip if no cards

        Card bobCard = bob.getHand().get(0);
        String suspect = bobCard.getName();

        // build a suggestion using Bob's card name as the suspect
        Suggestion suggestion = new Suggestion(
                new SuspectCard(suspect),
                new WeaponCard("Candlestick"),
                new RoomCard("Hall")
        );

        GameController.SuggestionResult result = controller.resolveSuggestion(suggestion);

        // if Bob holds the suspect card, he should disprove
        if (bob.getMatchingCards(suggestion).isEmpty()) {
            // Bob doesn't match this particular suggestion — that's OK
            return;
        }

        assertTrue(result.isDisproved(), "Bob should disprove the suggestion");
        assertEquals("Bob", result.getDisproverName());
    }

    /**
     * A correct accusation ends the game with the accuser winning.
     */
    @Test
    public void testCorrectAccusationWinsGame() {
        MurderEnvelope env = controller.getEnvelope();
        Accusation correct = new Accusation(
                env.getSuspect(), env.getWeapon(), env.getRoom());

        boolean result = controller.checkAccusation(correct);

        assertTrue(result, "Correct accusation should return true");
        assertTrue(controller.isGameOver(), "Game should be over");
        assertEquals(controller.getCurrentPlayer(), controller.getWinner());
    }

    /**
     * A wrong accusation eliminates the player but does not end the game
     * if other players remain active.
     */
    @Test
    public void testWrongAccusationEliminatesPlayer() {
        // ensure the accusation is wrong
        Accusation wrong = new Accusation(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Candlestick"),
                new RoomCard("Hall")
        );
        MurderEnvelope env = controller.getEnvelope();
        if (env.getSuspect().getName().equals("Miss Scarlett")
                && env.getWeapon().getName().equals("Candlestick")
                && env.getRoom().getName().equals("Hall")) {
            wrong = new Accusation(
                    new SuspectCard("Mrs White"),
                    new WeaponCard("Rope"),
                    new RoomCard("Kitchen")
            );
        }

        boolean result = controller.checkAccusation(wrong);

        assertFalse(result, "Wrong accusation should return false");
        assertFalse(controller.getCurrentPlayer().isActive(),
                "Accuser should be eliminated");
    }

    /**
     * isCurrentPlayerAI correctly identifies AI vs human players.
     */
    @Test
    public void testPlayerTypeDetection() {
        // first player is human (Alice)
        assertTrue(controller.isCurrentPlayerHuman());
        assertFalse(controller.isCurrentPlayerAI());

        // advance to AI player (Bob)
        controller.advanceToNextPlayer();
        assertTrue(controller.isCurrentPlayerAI());
        assertFalse(controller.isCurrentPlayerHuman());
    }

    /**
     * advanceToNextPlayer wraps around the player list correctly.
     */
    @Test
    public void testAdvanceWrapsAround() {
        // with 2 players: Alice (0), Bob (1)
        assertEquals("Alice", controller.getCurrentPlayer().getName());
        controller.advanceToNextPlayer();
        assertEquals("Bob", controller.getCurrentPlayer().getName());
        controller.advanceToNextPlayer();
        assertEquals("Alice", controller.getCurrentPlayer().getName());
    }

    /**
     * Human-only games should advance directly between human players without
     * relying on any AI continuation loop.
     */
    @Test
    public void testHumanOnlyAdvanceToNextHuman() {
        List<Player> humanPlayers = new ArrayList<>();
        humanPlayers.add(new HumanPlayer("Player 1", "Scarlett", 0, 0,
                new Scanner(System.in)));
        humanPlayers.add(new HumanPlayer("Player 2", "Mustard", 0, 0,
                new Scanner(System.in)));

        Game humanOnlyGame = new Game(humanPlayers);
        humanOnlyGame.startGame();
        GameController humanOnlyController = new GameController(humanOnlyGame);

        assertEquals("Player 1", humanOnlyController.getCurrentPlayer().getName());
        humanOnlyController.advanceToNextPlayer();
        assertEquals("Player 2", humanOnlyController.getCurrentPlayer().getName());
        assertFalse(humanOnlyController.isGameOver());
    }

    /**
     * Eliminated human players still hold cards, but their turns should be
     * skipped so the next active human can continue.
     */
    @Test
    public void testHumanOnlyAdvanceSkipsEliminatedHuman() {
        List<Player> humanPlayers = new ArrayList<>();
        humanPlayers.add(new HumanPlayer("Player 1", "Scarlett", 0, 0,
                new Scanner(System.in)));
        humanPlayers.add(new HumanPlayer("Player 2", "Mustard", 0, 0,
                new Scanner(System.in)));

        Game humanOnlyGame = new Game(humanPlayers);
        humanOnlyGame.startGame();
        GameController humanOnlyController = new GameController(humanOnlyGame);

        Accusation wrong = new Accusation(
                new SuspectCard("Miss Scarlett"),
                new WeaponCard("Candlestick"),
                new RoomCard("Kitchen"));
        MurderEnvelope envelope = humanOnlyController.getEnvelope();
        if (envelope.verify(wrong.getSuspect(), wrong.getWeapon(), wrong.getRoom())) {
            wrong = new Accusation(
                    new SuspectCard("Mrs White"),
                    new WeaponCard("Rope"),
                    new RoomCard("Hall"));
        }

        assertFalse(humanOnlyController.checkAccusation(wrong));
        assertFalse(humanOnlyController.getCurrentPlayer().isActive());

        humanOnlyController.advanceToNextPlayer();

        assertEquals("Player 2", humanOnlyController.getCurrentPlayer().getName());
        assertTrue(humanOnlyController.getCurrentPlayer().isActive());
        assertFalse(humanOnlyController.isGameOver());
    }

    /**
     * If an AI has already deduced the full envelope, it should accuse at the
     * start of its turn instead of waiting to enter another room first.
     */
    @Test
    public void testAIAccusesAtStartOfTurnWhenReady() {
        List<Player> testPlayers = new ArrayList<>();
        AIPlayer ai = new AIPlayer("AI Scarlett", "Scarlett", 0, 0);
        testPlayers.add(ai);
        testPlayers.add(new HumanPlayer("Player 2", "Mustard", 0, 0,
                new Scanner(System.in)));

        Game testGame = new Game(testPlayers);
        testGame.startGame();
        GameController testController = new GameController(testGame);
        MurderEnvelope envelope = testController.getEnvelope();

        ai.setUndisprovedSuggestion(new Suggestion(
                envelope.getSuspect(),
                envelope.getWeapon(),
                envelope.getRoom()));

        List<String> log = testController.runAITurn();

        assertTrue(testController.isGameOver());
        assertEquals(ai, testController.getWinner());
        assertTrue(log.stream().anyMatch(line -> line.contains("accuses")));
    }

    /**
     * AI players should disprove automatically in the GUI; the private card
     * chooser is only for human disprovers.
     */
    @Test
    public void testAIDisprovalDoesNotUsePrivateChooser() {
        List<Player> testPlayers = new ArrayList<>();
        AIPlayer suggester = new AIPlayer("AI Scarlett", "Scarlett", 0, 0);
        AIPlayer disprover = new AIPlayer("AI Mustard", "Mustard", 0, 0);
        disprover.addCard(new WeaponCard("Dagger"));
        testPlayers.add(suggester);
        testPlayers.add(disprover);

        Game testGame = new Game(testPlayers);
        testGame.startGame();
        GameController testController = new GameController(testGame);
        suggester.enterRoom(testGame.getBoard().getRoom("Hall"));
        testController.setSuggestionCardChooser((player, cards, asker) -> {
            fail("Private card chooser should not be used for AI disprovers");
            return cards.get(0);
        });

        GameController.SuggestionResult result = testController.resolveSuggestion(
                new Suggestion(
                        new SuspectCard("Mrs White"),
                        new WeaponCard("Dagger"),
                        new RoomCard("Hall")));

        assertTrue(result.isDisproved());
        assertEquals("AI Mustard", result.getDisproverName());
        assertEquals("Dagger", result.getShownCard().getName());
    }
}
