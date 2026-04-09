package com.cluegame.model;

import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.cards.RoomCard;

/**
 * Represents a formal accusation made by a player to end the game.
 * Extends Suggestion as it holds the same three-card structure, but carries
 * different game meaning — an accusation is checked directly against the
 * MurderEnvelope and ends the game if correct, or eliminates the player if wrong.
 * @author Thanh Shaw
 */
public class Accusation extends Suggestion {

    /**
     * Constructs an Accusation with the given suspect, weapon and room.
     * @param suspect the accused murderer
     * @param weapon the accused murder weapon
     * @param room the room the murder is accused to have occurred in
     */
    public Accusation(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        super(suspect, weapon, room);
    }
}
