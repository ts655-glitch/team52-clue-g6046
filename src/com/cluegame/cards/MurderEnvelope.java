package com.cluegame.cards;

/**
 * Holds the three secret murder cards. Hidden until a correct accusation is made.
 * @author Team 52
 */
public class MurderEnvelope {
    private SuspectCard suspect;
    private WeaponCard weapon;
    private RoomCard room;

    /**
     * Seals the envelope with the three murder solution cards.
     * @param suspect the murderer card
     * @param weapon the weapon card
     * @param room the room card
     */
    public void seal(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }

    /**
     * Verifies whether an accusation matches the envelope contents.
     * @param s suspect card
     * @param w weapon card
     * @param r room card
     * @return true if all three match
     */
    public boolean verify(SuspectCard s, WeaponCard w, RoomCard r) {
        return suspect.getName().equals(s.getName())
                && weapon.getName().equals(w.getName())
                && room.getName().equals(r.getName());
    }
}
