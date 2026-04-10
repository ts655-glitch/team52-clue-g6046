package com.cluegame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Tests for the Board class — layout, rooms, doors and movement validation.
 * @author Thanh Shaw
 */
public class BoardTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
    }

    /**
     * Validates FR4 — board layout must contain exactly 9 rooms.
     */
    @Test
    public void testBoardHasNineRooms() {
        Map<String, Room> rooms = board.getRooms();
        assertEquals(9, rooms.size(), "Board should have exactly 9 rooms");
    }

    /**
     * Validates FR4 — all 9 expected room names are present on the board.
     */
    @Test
    public void testAllRoomNamesPresent() {
        String[] expected = {
            "Study", "Hall", "Lounge", "Library", "Billiard Room",
            "Dining Room", "Conservatory", "Ballroom", "Kitchen"
        };
        Map<String, Room> rooms = board.getRooms();
        for (String name : expected) {
            assertNotNull(rooms.get(name), "Room '" + name + "' should exist on the board");
        }
    }

    /**
     * Validates FR13 — players cannot move onto blocked squares.
     * The central staircase area (row 10, col 11) is blocked.
     */
    @Test
    public void testIsValidMoveReturnsFalseForBlocked() {
        // (9, 11) is a corridor next to the staircase, (10, 11) is blocked
        assertFalse(board.isValidMove(9, 11, 10, 11),
                "Should not be able to move onto a blocked square");
    }

    /**
     * Validates FR13 — players cannot move outside the board boundaries.
     */
    @Test
    public void testIsValidMoveReturnsFalseForOutOfBounds() {
        assertFalse(board.isValidMove(0, 16, -1, 16),
                "Should not be able to move above the board");
        assertFalse(board.isValidMove(23, 7, 24, 7),
                "Should not be able to move below the board");
        assertFalse(board.isValidMove(7, 0, 7, -1),
                "Should not be able to move left of the board");
        assertFalse(board.isValidMove(7, 24, 7, 25),
                "Should not be able to move right of the board");
    }

    /**
     * Validates FR16 — Study has a secret passage leading to Kitchen.
     */
    @Test
    public void testSecretPassageStudyToKitchen() {
        Room study = board.getRoom("Study");
        assertTrue(study.hasSecretPassage(), "Study should have a secret passage");
        assertEquals("Kitchen", study.getSecretPassage().getName(),
                "Study's secret passage should lead to Kitchen");
    }

    /**
     * Validates FR16 — Kitchen has a secret passage leading back to Study.
     */
    @Test
    public void testSecretPassageKitchenToStudy() {
        Room kitchen = board.getRoom("Kitchen");
        assertTrue(kitchen.hasSecretPassage(), "Kitchen should have a secret passage");
        assertEquals("Study", kitchen.getSecretPassage().getName(),
                "Kitchen's secret passage should lead to Study");
    }

    /**
     * Validates FR16 — Conservatory has a secret passage leading to Lounge.
     */
    @Test
    public void testSecretPassageConservatoryToLounge() {
        Room conservatory = board.getRoom("Conservatory");
        assertTrue(conservatory.hasSecretPassage(),
                "Conservatory should have a secret passage");
        assertEquals("Lounge", conservatory.getSecretPassage().getName(),
                "Conservatory's secret passage should lead to Lounge");
    }

    /**
     * Validates FR16 — Lounge has a secret passage leading back to Conservatory.
     */
    @Test
    public void testSecretPassageLoungeToConservatory() {
        Room lounge = board.getRoom("Lounge");
        assertTrue(lounge.hasSecretPassage(), "Lounge should have a secret passage");
        assertEquals("Conservatory", lounge.getSecretPassage().getName(),
                "Lounge's secret passage should lead to Conservatory");
    }

    /**
     * Validates FR4 — every room on the board has at least one door.
     */
    @Test
    public void testEveryRoomHasAtLeastOneDoor() {
        for (Room room : board.getRooms().values()) {
            assertFalse(room.getDoors().isEmpty(),
                    room.getName() + " should have at least one door");
        }
    }

    /**
     * Validates FR14 — door squares are correctly identified by isDoor().
     * The Study door at (5, 3) should be detected as a door.
     */
    @Test
    public void testIsDoorDetectsDoorSquares() {
        assertTrue(board.isDoor(5, 3), "Study door at (5,3) should be identified as a door");
        assertFalse(board.isDoor(1, 16), "Plain corridor at (1,16) should not be a door");
        assertFalse(board.isDoor(10, 11), "Blocked square should not be a door");
    }

    /**
     * Validates FR4 — rooms without secret passages return false for hasSecretPassage().
     * Hall, Library, Billiard Room, Dining Room and Ballroom have no passages.
     */
    @Test
    public void testRoomsWithoutPassages() {
        String[] noPassage = {"Hall", "Library", "Billiard Room", "Dining Room", "Ballroom"};
        for (String name : noPassage) {
            Room room = board.getRoom(name);
            assertFalse(room.hasSecretPassage(),
                    name + " should not have a secret passage");
        }
    }
}
