package com.cluegame.model;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.cards.RoomCard;

/**
 * A formal accusation checked against the murder envelope.
 * Extends Suggestion as it uses the same three-card structure.
 * @author Team 52
 */
public class Accusation extends Suggestion {
    public Accusation(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        super(suspect, weapon, room);
    }
}