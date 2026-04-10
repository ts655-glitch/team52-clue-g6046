package com.cluegame.model;

import com.cluegame.players.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
     * Initialises the grid with the classic Cluedo board layout.
     * Places all 9 rooms, blocked wall squares, doors and the 6 starting
     * positions. A config file loaded via loadFromFile() can override this.
     */
    private void initialiseGrid() {
        // default everything to corridor first
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = new Square(row, col, Square.Type.CORRIDOR);
            }
        }

        // --- Room layouts (matching classic Clue! board) ---
        //
        //  Study(TL)        Hall(TC)        Lounge(TR)
        //  Library(ML)      [staircase]     Dining Room(MR)
        //  Billiard(ML)
        //  Conservatory(BL) Ballroom(BC)    Kitchen(BR)

        fillRoom("Study", 0, 0, 4, 6);
        fillRoom("Hall", 0, 9, 5, 14);
        fillRoom("Lounge", 0, 18, 4, 24);
        fillRoom("Library", 7, 0, 11, 5);
        fillRoom("Billiard Room", 13, 0, 17, 5);
        fillRoom("Dining Room", 9, 17, 14, 24);
        fillRoom("Conservatory", 19, 0, 23, 5);
        fillRoom("Ballroom", 18, 8, 23, 15);
        fillRoom("Kitchen", 18, 18, 23, 24);

        // --- Central staircase (blocked, not accessible) ---
        fillBlocked(10, 11, 13, 13);

        // --- Blocked wall squares along board edges ---

        // top edge between rooms
        fillBlocked(0, 7, 0, 8);
        fillBlocked(0, 15, 0, 17);
        // left edge between rooms
        fillBlocked(5, 0, 6, 0);     // between Study and Library
        fillBlocked(12, 0, 12, 0);   // between Library and Billiard Room
        fillBlocked(18, 0, 18, 0);   // between Billiard Room and Conservatory
        // right edge between rooms
        fillBlocked(5, 24, 8, 24);   // between Lounge and Dining Room
        fillBlocked(15, 24, 17, 24); // below Dining Room
        // bottom edge between rooms
        fillBlocked(23, 6, 23, 7);   // between Conservatory and Ballroom
        fillBlocked(23, 16, 23, 17); // between Ballroom and Kitchen

        // --- Door squares (corridor squares adjacent to rooms) ---

        addDoor("Study", 5, 3);          // south side
        addDoor("Hall", 6, 11);          // south-left
        addDoor("Hall", 6, 12);          // south-right
        addDoor("Lounge", 5, 18);        // south side
        addDoor("Library", 6, 3);        // north side
        addDoor("Library", 12, 3);       // south side
        addDoor("Billiard Room", 12, 1); // north side
        addDoor("Billiard Room", 18, 3); // south side
        addDoor("Dining Room", 11, 16);  // west side
        addDoor("Dining Room", 15, 20);  // south side
        addDoor("Conservatory", 18, 4);  // north side
        addDoor("Ballroom", 17, 9);      // north-left
        addDoor("Ballroom", 17, 14);     // north-right
        addDoor("Kitchen", 17, 20);      // north side

        // --- Starting positions for 6 players ---
        // Matches classic board layout (see board image)
        setStart(0, 16);  // Miss Scarlett (top, between Hall and Lounge)
        setStart(7, 24);  // Colonel Mustard (right, between Lounge and Dining Room)
        setStart(23, 16); // Mrs White (bottom-right, near Kitchen)
        setStart(23, 7);  // Reverend Green (bottom-left, near Conservatory)
        setStart(18, 0);  // Mrs Peacock (left, between Billiard Room and Conservatory)
        setStart(5, 0);   // Professor Plum (left, between Study and Library)
    }

    /**
     * Fills a rectangular region with ROOM squares and links them to the
     * named Room object.
     * @param roomName the room to assign these squares to
     * @param r1 top row of the rectangle (inclusive)
     * @param c1 left column (inclusive)
     * @param r2 bottom row (inclusive)
     * @param c2 right column (inclusive)
     */
    private void fillRoom(String roomName, int r1, int c1, int r2, int c2) {
        Room room = rooms.get(roomName);
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                grid[r][c] = new Square(r, c, Square.Type.ROOM);
                grid[r][c].setRoom(room);
            }
        }
    }

    /**
     * Fills a rectangular region with BLOCKED squares.
     * @param r1 top row (inclusive)
     * @param c1 left column (inclusive)
     * @param r2 bottom row (inclusive)
     * @param c2 right column (inclusive)
     */
    private void fillBlocked(int r1, int c1, int r2, int c2) {
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                grid[r][c] = new Square(r, c, Square.Type.BLOCKED);
            }
        }
    }

    /**
     * Marks a corridor square as a door for the given room and registers
     * it with the Room object. The door square itself stays as CORRIDOR
     * so players can stand on it.
     * @param roomName the room this door belongs to
     * @param row door row
     * @param col door column
     */
    private void addDoor(String roomName, int row, int col) {
        // door squares are corridors that players walk through to enter a room
        grid[row][col] = new Square(row, col, Square.Type.CORRIDOR);
        grid[row][col].setRoom(rooms.get(roomName));
        rooms.get(roomName).addDoor(grid[row][col]);
    }

    /**
     * Marks a square as a player starting position.
     * @param row start row
     * @param col start column
     */
    private void setStart(int row, int col) {
        grid[row][col] = new Square(row, col, Square.Type.START);
    }

    // short labels for each room used in printBoard() output
    private static final Map<String, Character> ROOM_LABELS = Map.of(
        "Kitchen", 'K',
        "Ballroom", 'A',
        "Conservatory", 'C',
        "Dining Room", 'D',
        "Billiard Room", 'B',
        "Library", 'I',
        "Lounge", 'O',
        "Hall", 'H',
        "Study", 'U'
    );

    /**
     * Prints a text representation of the board to standard output.
     * Each room is shown with a unique letter (see legend below the grid).
     * Corridors = '.', blocked = '#', start = 'S', doors = '*'.
     */
    public void printBoard() {
        System.out.print("   ");
        for (int col = 0; col < COLS; col++) {
            System.out.printf("%2d", col);
        }
        System.out.println();

        for (int row = 0; row < ROWS; row++) {
            System.out.printf("%2d ", row);
            for (int col = 0; col < COLS; col++) {
                Square sq = grid[row][col];
                char ch;
                if (sq.getType() == Square.Type.ROOM && sq.getRoom() != null) {
                    ch = ROOM_LABELS.getOrDefault(sq.getRoom().getName(), 'R');
                } else if (sq.getType() == Square.Type.BLOCKED) {
                    ch = '#';
                } else if (sq.getType() == Square.Type.START) {
                    ch = 'S';
                } else if (sq.getRoom() != null) {
                    // corridor square that is a door
                    ch = '*';
                } else {
                    ch = '.';
                }
                System.out.print(" " + ch);
            }
            System.out.println();
        }

        // print room legend
        System.out.println("\nRoom legend:");
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            Room room = entry.getValue();
            char label = ROOM_LABELS.getOrDefault(room.getName(), '?');
            String passage = room.hasSecretPassage()
                ? " [passage -> " + room.getSecretPassage().getName() + "]"
                : "";
            System.out.println("  " + label + " = " + room.getName()
                + " (" + room.getDoors().size() + " doors)" + passage);
        }
        System.out.println("  * = door, S = start, # = blocked, . = corridor");
    }

    /**
     * Prints the board with player positions overlaid. Players in corridors
     * are shown as numbered tokens (1-6). Players in rooms are listed in
     * a summary below the grid.
     * @param players the list of players to display on the board
     */
    public void printBoard(List<Player> players) {
        // build a lookup of corridor positions to player number
        Map<String, Integer> playerPositions = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.getCurrentRoom() == null) {
                playerPositions.put(p.getRow() + "," + p.getCol(), i + 1);
            }
        }

        System.out.print("   ");
        for (int col = 0; col < COLS; col++) {
            System.out.printf("%2d", col);
        }
        System.out.println();

        for (int row = 0; row < ROWS; row++) {
            System.out.printf("%2d ", row);
            for (int col = 0; col < COLS; col++) {
                String key = row + "," + col;
                char ch;

                // player token takes priority on corridor squares
                if (playerPositions.containsKey(key)) {
                    ch = (char) ('0' + playerPositions.get(key));
                } else {
                    Square sq = grid[row][col];
                    if (sq.getType() == Square.Type.ROOM && sq.getRoom() != null) {
                        ch = ROOM_LABELS.getOrDefault(sq.getRoom().getName(), 'R');
                    } else if (sq.getType() == Square.Type.BLOCKED) {
                        ch = '#';
                    } else if (sq.getType() == Square.Type.START) {
                        ch = 'S';
                    } else if (sq.getRoom() != null) {
                        ch = '*';
                    } else {
                        ch = '.';
                    }
                }
                System.out.print(" " + ch);
            }
            System.out.println();
        }

        // print player positions summary
        System.out.println("\nPlayers:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = p.isActive() ? "" : " [ELIMINATED]";
            String location;
            if (p.getCurrentRoom() != null) {
                location = "in " + p.getCurrentRoom().getName();
            } else {
                location = "at (" + p.getRow() + ", " + p.getCol() + ")";
            }
            System.out.println("  " + (i + 1) + ") " + p.getName()
                    + " (" + p.getToken() + ") — " + location + status);
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

    /**
     * Checks whether moving from one square to another is valid.
     * A move is valid if the destination is within bounds, exactly one step
     * horizontally or vertically from the source, and not a BLOCKED square.
     * Does not check player collisions — use the overloaded version for that.
     * @param fromRow current row
     * @param fromCol current column
     * @param toRow destination row
     * @param toCol destination column
     * @return true if the move is valid
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow < 0 || toRow >= ROWS || toCol < 0 || toCol >= COLS) {
            return false;
        }
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (!((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1))) {
            return false;
        }
        return grid[toRow][toCol].getType() != Square.Type.BLOCKED;
    }

    /**
     * Checks whether moving from one square to another is valid, including
     * player collision. Same rules as the basic version but also blocks
     * moves into ROOM squares (must enter via doors) and into corridor
     * squares occupied by another player.
     * @param fromRow current row
     * @param fromCol current column
     * @param toRow destination row
     * @param toCol destination column
     * @param allPlayers all players for corridor occupancy checks
     * @return true if the move is valid
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol,
                               List<Player> allPlayers) {
        if (toRow < 0 || toRow >= ROWS || toCol < 0 || toCol >= COLS) {
            return false;
        }
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (!((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1))) {
            return false;
        }

        Square dest = grid[toRow][toCol];

        // cannot enter blocked or room-interior squares (rooms entered via doors)
        if (dest.getType() == Square.Type.BLOCKED || dest.getType() == Square.Type.ROOM) {
            return false;
        }

        // corridor/start/door squares: check not occupied by another player in a corridor
        for (Player p : allPlayers) {
            if (p.isActive() && p.getCurrentRoom() == null
                    && p.getRow() == toRow && p.getCol() == toCol) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the square at the given position is a door.
     * A door is a CORRIDOR square that has a room reference set on it.
     * @param row the row to check
     * @param col the column to check
     * @return true if the square is a door
     */
    public boolean isDoor(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return false;
        }
        Square sq = grid[row][col];
        return sq.getType() == Square.Type.CORRIDOR && sq.getRoom() != null;
    }

    /**
     * Returns the Room associated with the square at the given position,
     * or null if the square has no room (e.g. plain corridor, blocked).
     * Works for both door squares and room-interior squares.
     * @param row the row
     * @param col the column
     * @return the Room at that position, or null
     */
    public Room getRoomAt(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return null;
        }
        return grid[row][col].getRoom();
    }
}