package com.cluegame.cards;

/**
 * Abstract base class representing a Clue! game card.
 * All cards have a name and a type.
 * @author Team 52
 */
public abstract class Card {

    /**
     * The type of card.
     */
    public enum CardType {
        SUSPECT, WEAPON, ROOM
    }

    private String name;
    private CardType cardType;

    /**
     * Constructs a card with the given name and type.
     * @param name the name on the card
     * @param cardType the type of this card
     */
    public Card(String name, CardType cardType) {
        this.name = name;
        this.cardType = cardType;
    }

    public String getName() { return name; }
    public CardType getCardType() { return cardType; }

    @Override
    public String toString() {
        return cardType + ": " + name;
    }
}