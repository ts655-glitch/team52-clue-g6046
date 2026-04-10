package com.cluegame.cards;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MurderEnvelope class.
 * @author Thanh Shaw
 */
public class MurderEnvelopeTest {

    /**
     * Seals the envelope then calls verify() with matching cards — expects true.
     * Covers requirement: a correct accusation is recognised as the solution.
     */
    @Test
    public void testVerifyReturnsTrueForCorrectCards() {
        MurderEnvelope envelope = new MurderEnvelope();
        SuspectCard suspect = new SuspectCard("Miss Scarlett");
        WeaponCard weapon = new WeaponCard("Dagger");
        RoomCard room = new RoomCard("Kitchen");
        envelope.seal(suspect, weapon, room);

        assertTrue(envelope.verify(
            new SuspectCard("Miss Scarlett"),
            new WeaponCard("Dagger"),
            new RoomCard("Kitchen")
        ));
    }

    /**
     * Seals the envelope then calls verify() with a wrong suspect — expects false.
     * Covers requirement: a wrong accusation is rejected.
     */
    @Test
    public void testVerifyReturnsFalseForWrongSuspect() {
        MurderEnvelope envelope = new MurderEnvelope();
        envelope.seal(
            new SuspectCard("Miss Scarlett"),
            new WeaponCard("Dagger"),
            new RoomCard("Kitchen")
        );

        assertFalse(envelope.verify(
            new SuspectCard("Colonel Mustard"),
            new WeaponCard("Dagger"),
            new RoomCard("Kitchen")
        ));
    }

    /**
     * Seals the envelope then calls verify() with a wrong weapon — expects false.
     * Covers requirement: all three cards must match for an accusation to succeed.
     */
    @Test
    public void testVerifyReturnsFalseForWrongWeapon() {
        MurderEnvelope envelope = new MurderEnvelope();
        envelope.seal(
            new SuspectCard("Miss Scarlett"),
            new WeaponCard("Dagger"),
            new RoomCard("Kitchen")
        );

        assertFalse(envelope.verify(
            new SuspectCard("Miss Scarlett"),
            new WeaponCard("Rope"),
            new RoomCard("Kitchen")
        ));
    }

    /**
     * Checks isSealed() returns true once seal() has been called with valid cards.
     * Covers requirement: envelope must be sealed before the game starts.
     */
    @Test
    public void testIsSealedAfterSealing() {
        MurderEnvelope envelope = new MurderEnvelope();
        assertFalse(envelope.isSealed(), "Envelope should not be sealed initially");

        envelope.seal(
            new SuspectCard("Professor Plum"),
            new WeaponCard("Candlestick"),
            new RoomCard("Study")
        );

        assertTrue(envelope.isSealed(), "Envelope should be sealed after seal() is called");
    }
}
