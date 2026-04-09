package com.cluegame.cards;

/**
 * Represents the murder envelope containing the solution to the crime.
 * Holds exactly one suspect, one weapon and one room card.
 * @author Thanh Shaw
 */
public class MurderEnvelope {

    private SuspectCard suspect;
    private WeaponCard weapon;
    private RoomCard room;

    /**
     * Constructs an empty murder envelope.
     */
    public MurderEnvelope() {
        this.suspect = null;
        this.weapon = null;
        this.room = null;
    }

    /**
     * Seals the envelope with the three solution cards.
     * @param suspect the murderer
     * @param weapon the weapon used
     * @param room the room the crime was committed in
     */
    public void seal(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    /**
     * Checks whether a given accusation matches the envelope contents.
     * @param suspect the accused suspect
     * @param weapon the accused weapon
     * @param room the accused room
     * @return true if all three match, false otherwise
     */
    public boolean verify(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        return this.suspect.getName().equals(suspect.getName()) &&
                this.weapon.getName().equals(weapon.getName()) &&
                this.room.getName().equals(room.getName());
    }

    public SuspectCard getSuspect() { return suspect; }
    public WeaponCard getWeapon() { return weapon; }
    public RoomCard getRoom() { return room; }

    /**
     * Returns true if the envelope has been sealed.
     * @return true if sealed
     */
    public boolean isSealed() {
        return suspect != null && weapon != null && room != null;
    }
}