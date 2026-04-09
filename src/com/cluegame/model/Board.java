package com.cluegame.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Clue! game board.
 * The board is a 24x25 grid of squares loaded from an external config file.
 * @author Thanh Shaw
 */
public class Board {

    public static final int ROWS = 24;
    public static final int COLS = 25;

    private Square[][] grid;
    private Map<String, Room> rooms;

    /**
     * Constructs a new Board and initialises all rooms.
     */
    public Board() {
        this.grid = new Square[ROWS][COLS];
        this.rooms = new HashMap<>();
        initialiseRooms();
        initialiseGrid();
    }

    /**
     * Creates all 9 rooms on the board.
     */
    private void initialiseRooms() {
        String[] roomNames = {
                "Kitchen", "Ballroom", "Conservatory",
                "Billiard Room", "Library", "Study",
                "Hall", "Lounge", "Dining Room"
        };
        for (String name : roomNames) {
            rooms.put(name, new Room(name));
        }
        // Secret passages (diagonal corners of the board)
        rooms.get("Kitchen").setSecretPassage(rooms.get("Study"));
        rooms.get("Study").setSecretPassage(rooms.get("Kitchen"));
        rooms.get("Conservatory").setSecretPassage(rooms.get("Lounge"));
        rooms.get("Lounge").setSecretPassage(rooms.get("Conservatory"));
    }

    /**
     * Initialises the grid — all squares default to CORRIDOR.
     * A config file can override this layout.
     */
    private void initialiseGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = new Square(row, col, Square.Type.CORRIDOR);
            }
        }
    }

    /**
     * Loads the board layout from an external text config file.
     * Each character in the file maps to a square type:
     * C = Corridor, R = Room, B = Blocked, S = Start
     * @param filePath path to the board config file
     */
    public void loadFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null && row < ROWS) {
                for (int col = 0; col < line.length() && col < COLS; col++) {
                    char c = line.charAt(col);
                    Square.Type type = switch (c) {
                        case 'R' -> Square.Type.ROOM;
                        case 'B' -> Square.Type.BLOCKED;
                        case 'S' -> Square.Type.START;
                        default -> Square.Type.CORRIDOR;
                    };
                    grid[row][col] = new Square(row, col, type);
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("Could not load board config: " + e.getMessage());
        }
    }

    /**
     * Returns the square at the given position.
     * @param row the row index
     * @param col the column index
     * @return the Square at that position
     */
    public Square getSquare(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns a room by name.
     * @param name the room name
     * @return the Room object
     */
    public Room getRoom(String name) {
        return rooms.get(name);
    }

    /**
     * Returns all rooms on the board.
     * @return map of room names to Room objects
     */
    public Map<String, Room> getRooms() {
        return rooms;
    }
}