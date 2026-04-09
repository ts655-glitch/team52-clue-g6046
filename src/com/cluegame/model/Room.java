package com.cluegame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room on the Clue! board.
 * Rooms have a name, a list of door squares, and an optional secret passage.
 * @author Thanh Shaw
 */
public class Room {

    private String name;
    private List<Square> doors;
    private Room secretPassage; // null if no secret passage
    private List<String> occupants; // player names currently in this room

    /**
     * Constructs a room with the given name.
     * @param name the name of the room
     */
    public Room(String name) {
        this.name = name;
        this.doors = new ArrayList<>();
        this.occupants = new ArrayList<>();
        this.secretPassage = null;
    }

    /**
     * Adds a door square to this room.
     * @param door the square that acts as a door
     */
    public void addDoor(Square door) {
        doors.add(door);
    }

    /**
     * Sets the secret passage destination for this room.
     * @param room the room this secret passage leads to
     */
    public void setSecretPassage(Room room) {
        this.secretPassage = room;
    }

    public String getName() { return name; }
    public List<Square> getDoors() { return doors; }
    public Room getSecretPassage() { return secretPassage; }
    public boolean hasSecretPassage() { return secretPassage != null; }
    public List<String> getOccupants() { return occupants; }
    public void addOccupant(String playerName) { occupants.add(playerName); }
    public void removeOccupant(String playerName) { occupants.remove(playerName); }
}