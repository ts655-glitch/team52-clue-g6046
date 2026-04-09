package com.cluegame.cards;

/**
 * Represents a room card in Clue!
 * There are 9 room cards in the game.
 * @author Thanh Shaw
 */
public class RoomCard extends Card {

    /**
     * Constructs a room card with the given name.
     * @param name the name of the room
     */
    public RoomCard(String name) {
        super(name, CardType.ROOM);
    }
}