package com.cluegame.model;

import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.cards.RoomCard;

/**
 * Represents a suggestion made by a player during their turn.
 * A suggestion names a suspect, a weapon and the room the player is currently in.
 * Other players must disprove it by showing a matching card if they can.
 * @author Thanh Shaw
 */
public class Suggestion {

    private SuspectCard suspect;
    private WeaponCard weapon;
    private RoomCard room;

    /**
     * Constructs a Suggestion with the given suspect, weapon and room.
     * @param suspect the suspected murderer
     * @param weapon the suspected murder weapon
     * @param room the room the murder is suggested to have occurred in
     */
    public Suggestion(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    /**
     * Returns the suspected murderer.
     * @return the suspect card
     */
    public SuspectCard getSuspect() { return suspect; }

    /**
     * Returns the suspected murder weapon.
     * @return the weapon card
     */
    public WeaponCard getWeapon() { return weapon; }

    /**
     * Returns the room the murder is suggested to have occurred in.
     * @return the room card
     */
    public RoomCard getRoom() { return room; }

    /**
     * Returns a human-readable description of this suggestion.
     * @return string in the format "Suspect in the Room with the Weapon"
     */
    @Override
    public String toString() {
        return suspect.getName() + " in the " + room.getName() + " with the " + weapon.getName();
    }
}
