package com.cluegame.model;

/**
 * Represents a single cell on the game board.
 * @author Team 52
 */
public class Square {

    public enum Type {
        CORRIDOR, ROOM, BLOCKED, START
    }

    private int row;
    private int col;
    private Type type;
    private Room room;
    private boolean occupied;

    /**
     * Constructs a square at the given position with the given type.
     * @param row the row index
     * @param col the column index
     * @param type the type of this square
     */
    public Square(int row, int col, Type type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.occupied = false;
        this.room = null;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Type getType() { return type; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}