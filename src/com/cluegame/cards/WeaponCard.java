package com.cluegame.cards;

/**
 * Represents a weapon card in Clue!
 * There are 6 weapon cards in the game.
 * @author Thanh Shaw
 */
public class WeaponCard extends Card {

    /**
     * Constructs a weapon card with the given name.
     * @param name the name of the weapon
     */
    public WeaponCard(String name) {
        super(name, CardType.WEAPON);
    }
}