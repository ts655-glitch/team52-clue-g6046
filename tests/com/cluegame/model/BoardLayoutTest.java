package com.cluegame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the revised board layout. Verifies that room
 * boundaries, doors, blocked squares and start positions match the
 * board.png image after the Sprint 3 layout rework.
 * @author Thanh Shaw
 */
public class BoardLayoutTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
    }

    /**
     * Board dimensions should be 25 rows by 24 columns.
     */
    @Test
    public void testBoardDimensions() {
        assertEquals(25, Board.ROWS, "Board should have 25 rows");
        assertEquals(24, Board.COLS, "Board should have 24 columns");
    }

    /**
     * All door positions from the revised layout should be detected as doors.
     */
    @Test
    public void testAllDoorsAreDetected() {
        int[][] expectedDoors = {
            {4, 6},   // Study
            {7, 11},  // Hall south-left
            {7, 12},  // Hall south-right
            {4, 8},   // Hall west
            {6, 17},  // Lounge
            {8, 7},   // Library east
            {11, 3},  // Library south
            {15, 6},  // Billiard Room east
            {12, 15}, // Dining Room west
            {8, 17},  // Dining Room north
            {19, 5},  // Conservatory
            {16, 9},  // Ballroom north-left
            {16, 14}, // Ballroom north-right
            {19, 7},  // Ballroom west
            {19, 16}, // Ballroom east
            {17, 19}  // Kitchen
        };
        for (int[] pos : expectedDoors) {
            assertTrue(board.isDoor(pos[0], pos[1]),
                    "Expected door at (" + pos[0] + "," + pos[1] + ")");
        }
    }

    /**
     * Each door should be associated with the correct room.
     */
    @Test
    public void testDoorRoomAssociations() {
        assertEquals("Study", board.getRoomAt(4, 6).getName());
        assertEquals("Hall", board.getRoomAt(7, 11).getName());
        assertEquals("Hall", board.getRoomAt(4, 8).getName());
        assertEquals("Lounge", board.getRoomAt(6, 17).getName());
        assertEquals("Dining Room", board.getRoomAt(8, 17).getName());
        assertEquals("Kitchen", board.getRoomAt(17, 19).getName());
        assertEquals("Ballroom", board.getRoomAt(16, 9).getName());
        assertEquals("Conservatory", board.getRoomAt(19, 5).getName());
    }

    /**
     * Room interior squares should be identified as ROOM type.
     */
    @Test
    public void testRoomInteriorSquares() {
        // Study center
        assertEquals(Square.Type.ROOM, board.getSquare(1, 3).getType(),
                "(1,3) should be Study room interior");
        // Hall center
        assertEquals(Square.Type.ROOM, board.getSquare(3, 11).getType(),
                "(3,11) should be Hall room interior");
        // Kitchen center
        assertEquals(Square.Type.ROOM, board.getSquare(20, 20).getType(),
                "(20,20) should be Kitchen room interior");
    }

    /**
     * Key corridor squares should not be blocked or room squares.
     */
    @Test
    public void testCorridorSquaresAreOpen() {
        // corridor between Study and Hall on top row
        assertEquals(Square.Type.CORRIDOR, board.getSquare(0, 7).getType(),
                "(0,7) should be corridor");
        // corridor between Hall and Lounge
        assertNotEquals(Square.Type.BLOCKED, board.getSquare(1, 15).getType(),
                "(1,15) should not be blocked");
        // corridor below Library
        assertEquals(Square.Type.CORRIDOR, board.getSquare(11, 1).getType(),
                "(11,1) should be corridor");
    }

    /**
     * Central staircase area should be blocked.
     */
    @Test
    public void testStaircaseIsBlocked() {
        for (int row = 8; row <= 14; row++) {
            for (int col = 9; col <= 13; col++) {
                assertEquals(Square.Type.BLOCKED, board.getSquare(row, col).getType(),
                        "Staircase at (" + row + "," + col + ") should be blocked");
            }
        }
    }

    /**
     * Edge wall squares between rooms should be blocked.
     */
    @Test
    public void testEdgeWallsAreBlocked() {
        // top edge: (0,8) between Study and Hall
        assertEquals(Square.Type.BLOCKED, board.getSquare(0, 8).getType(),
                "(0,8) should be blocked wall");
        // left edge: (4,0) between Study and Plum start
        assertEquals(Square.Type.BLOCKED, board.getSquare(4, 0).getType(),
                "(4,0) should be blocked wall");
        // right edge: (7,23) is Mustard's start, but (6,23) and (8,23) should be blocked
        assertEquals(Square.Type.BLOCKED, board.getSquare(6, 23).getType(),
                "(6,23) should be blocked wall");
    }

    /**
     * Start positions should be accessible (not blocked or room interior).
     */
    @Test
    public void testStartPositionsAreAccessible() {
        int[][] starts = {{0, 16}, {7, 23}, {24, 14}, {24, 9}, {18, 0}, {5, 0}};
        for (int[] s : starts) {
            Square sq = board.getSquare(s[0], s[1]);
            assertNotEquals(Square.Type.BLOCKED, sq.getType(),
                    "Start at (" + s[0] + "," + s[1] + ") should not be blocked");
            assertNotEquals(Square.Type.ROOM, sq.getType(),
                    "Start at (" + s[0] + "," + s[1] + ") should not be room interior");
        }
    }

    /**
     * Bottom start squares (Green and White) should only allow movement
     * upward into the corridor, not sideways along row 24.
     */
    @Test
    public void testBottomStartsOnlyMoveUp() {
        // Green at (24,9) — should only be able to move up to (23,9)
        assertTrue(board.isValidMove(24, 9, 23, 9),
                "Green start should allow moving up");
        assertFalse(board.isValidMove(24, 9, 24, 8),
                "Green start should not allow moving left");
        assertFalse(board.isValidMove(24, 9, 24, 10),
                "Green start should not allow moving right");

        // White at (24,14) — should only be able to move up to (23,14)
        assertTrue(board.isValidMove(24, 14, 23, 14),
                "White start should allow moving up");
        assertFalse(board.isValidMove(24, 14, 24, 13),
                "White start should not allow moving left");
        assertFalse(board.isValidMove(24, 14, 24, 15),
                "White start should not allow moving right");
    }

    /**
     * Bottom row cells adjacent to start squares should be blocked.
     */
    @Test
    public void testBottomRowMostlyBlocked() {
        // cells next to Green start (24,9)
        assertEquals(Square.Type.BLOCKED, board.getSquare(24, 8).getType(),
                "(24,8) should be blocked");
        assertEquals(Square.Type.BLOCKED, board.getSquare(24, 10).getType(),
                "(24,10) should be blocked");

        // cells next to White start (24,14)
        assertEquals(Square.Type.BLOCKED, board.getSquare(24, 13).getType(),
                "(24,13) should be blocked");
        assertEquals(Square.Type.BLOCKED, board.getSquare(24, 15).getType(),
                "(24,15) should be blocked");
    }
}
