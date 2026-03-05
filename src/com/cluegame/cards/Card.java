package com.cluegame.cards;

/** Abstract base class for all cards. @author Team 52 */
public abstract class Card {
    public enum CardType { SUSPECT, WEAPON, ROOM }
    private String name;
    private CardType type;

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public CardType getType() { return type; }

    @Override
    public String toString() { return name; }
}