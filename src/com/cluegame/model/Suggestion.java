package com.cluegame.model;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.cards.RoomCard;

/** Represents a suggestion made by a player during their turn. @author Team 52 */
public class Suggestion {
    private SuspectCard suspect;
    private WeaponCard weapon;
    private RoomCard room;

    public Suggestion(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    public SuspectCard getSuspect() { return suspect; }
    public WeaponCard getWeapon() { return weapon; }
    public RoomCard getRoom() { return room; }

    @Override
    public String toString() {
        return suspect.getName() + " in the " + room.getName() + " with the " + weapon.getName();
    }
}