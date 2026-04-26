package com.cluegame.model;

import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the movement system — valid moves, player collision and room entry.
 * @author Thanh Shaw
 */
public class MovementTest {

    private Board board;
    private List<Player> players;

    @BeforeEach
    public void setUp() {
        board = new Board();
        players = new ArrayList<>();
    }

    /**
     * Validates FR6 — a player on a start square can make at least one valid move.
     * Miss Scarlett starts at (0, 16) and should be able to move south.
     */
    @Test
    public void testPlayerOnStartCanMove() {
        Player scarlett = new AIPlayer("Scarlett", "Scarlett", 0, 16);
        players.add(scarlett);

        // from start (0,16) the only valid direction is south to (1,16)
        assertTrue(board.isValidMove(0, 16, 1, 16, players),
                "Player on start square should be able to move south");
    }

    /**
     * Validates FR15 — no two players can occupy the same corridor square.
     * A move to a square occupied by another player should be rejected.
     */
    @Test
    public void testCannotMoveToOccupiedCorridorSquare() {
        Player alice = new AIPlayer("Alice", "Scarlett", 1, 16);
        Player bob = new AIPlayer("Bob", "Mustard", 2, 16);
        players.add(alice);
        players.add(bob);

        // alice at (1,16), bob at (2,16) — alice should not be able to move south
        assertFalse(board.isValidMove(1, 16, 2, 16, players),
                "Should not be able to move to a square occupied by another player");
    }

    /**
     * Validates FR15 — a player in a room does not block the corridor square
     * they entered through. Rooms can hold any number of players.
     */
    @Test
    public void testPlayerInRoomDoesNotBlockCorridor() {
        // Study door is now at (4,6)
        Player alice = new AIPlayer("Alice", "Scarlett", 4, 6);
        Player bob = new AIPlayer("Bob", "Mustard", 4, 7);
        players.add(alice);
        players.add(bob);

        // alice is at (4,6) which is the Study door — put her in the room
        alice.enterRoom(board.getRoom("Study"));

        // bob at (4,7) should be able to move west to (4,6) since alice is "in the room"
        assertTrue(board.isValidMove(4, 7, 4, 6, players),
                "Player in a room should not block the door corridor square");
    }

    /**
     * Validates FR13 — players cannot move diagonally, only horizontally or vertically.
     */
    @Test
    public void testCannotMoveDiagonally() {
        Player p = new AIPlayer("Test", "Scarlett", 1, 16);
        players.add(p);

        assertFalse(board.isValidMove(1, 16, 2, 17, players),
                "Diagonal movement should not be allowed");
    }

    /**
     * Validates FR14 — players cannot walk directly into room squares, they
     * must enter through doors. A room square adjacent to a corridor should
     * be blocked for direct movement.
     */
    @Test
    public void testCannotMoveIntoRoomSquareDirectly() {
        // Billiard Room is rows 12-16, cols 0-5.
        // (14, 6) is corridor, (14, 5) is Billiard Room interior.
        Player p = new AIPlayer("Test", "Scarlett", 14, 6);
        players.add(p);

        assertFalse(board.isValidMove(14, 6, 14, 5, players),
                "Should not be able to walk directly into a room square");
    }

    /**
     * Validates FR14 — entering a room sets the player's current room correctly.
     */
    @Test
    public void testEnterRoomSetsCurrentRoom() {
        // Study door is at (4,6)
        Player p = new AIPlayer("Test", "Scarlett", 4, 6);
        players.add(p);

        Room study = board.getRoom("Study");
        assertNull(p.getCurrentRoom(), "Player should not be in a room initially");

        p.enterRoom(study);
        assertEquals(study, p.getCurrentRoom(),
                "After entering, player should be in the Study");
        assertTrue(study.getOccupants().contains("Test"),
                "Room should list the player as an occupant");
    }

    /**
     * Validates FR14/FR16 — leaving a room clears the player's room and
     * removes them from the room's occupant list.
     */
    @Test
    public void testLeaveRoomClearsCurrentRoom() {
        Player p = new AIPlayer("Test", "Scarlett", 4, 6);
        Room study = board.getRoom("Study");
        p.enterRoom(study);

        p.leaveRoom();
        assertNull(p.getCurrentRoom(), "Player should not be in a room after leaving");
        assertFalse(study.getOccupants().contains("Test"),
                "Room should not list the player after they leave");
    }

    /**
     * Validates FR6 — all 6 starting positions allow at least one valid move.
     * Players should never be stuck on their starting square.
     */
    @Test
    public void testAllStartPositionsHaveValidMoves() {
        int[][] starts = {{0, 16}, {7, 23}, {24, 14}, {24, 9}, {18, 0}, {5, 0}};
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        for (int[] start : starts) {
            boolean hasMove = false;
            for (int d = 0; d < 4; d++) {
                if (board.isValidMove(start[0], start[1],
                        start[0] + dRow[d], start[1] + dCol[d])) {
                    hasMove = true;
                    break;
                }
            }
            assertTrue(hasMove, "Start position (" + start[0] + ", " + start[1]
                    + ") should have at least one valid move");
        }
    }
}
