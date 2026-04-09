package com.cluegame.cards;

/**
 * Represents a suspect card in Clue!
 * There are 6 suspect cards in the game.
 * @author Thanh Shaw
 */
public class SuspectCard extends Card {

    /**
     * Constructs a suspect card with the given name.
     * @param name the name of the suspect
     */
    public SuspectCard(String name) {
        super(name, CardType.SUSPECT);
    }
}