package com.cluegame.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the full deck of 21 Clue! cards.
 * Handles shuffling, sealing the murder envelope, and dealing to players.
 * @author Thanh Shaw
 */
public class Deck {

    private List<Card> cards;

    /**
     * Constructs a full deck of 21 cards:
     * 6 suspects, 6 weapons and 9 rooms.
     */
    public Deck() {
        cards = new ArrayList<>();

        // 6 Suspects
        cards.add(new SuspectCard("Miss Scarlett"));
        cards.add(new SuspectCard("Colonel Mustard"));
        cards.add(new SuspectCard("Mrs White"));
        cards.add(new SuspectCard("Reverend Green"));
        cards.add(new SuspectCard("Mrs Peacock"));
        cards.add(new SuspectCard("Professor Plum"));

        // 6 Weapons
        cards.add(new WeaponCard("Candlestick"));
        cards.add(new WeaponCard("Knife"));
        cards.add(new WeaponCard("Lead Pipe"));
        cards.add(new WeaponCard("Revolver"));
        cards.add(new WeaponCard("Rope"));
        cards.add(new WeaponCard("Wrench"));

        // 9 Rooms
        cards.add(new RoomCard("Kitchen"));
        cards.add(new RoomCard("Ballroom"));
        cards.add(new RoomCard("Conservatory"));
        cards.add(new RoomCard("Billiard Room"));
        cards.add(new RoomCard("Library"));
        cards.add(new RoomCard("Study"));
        cards.add(new RoomCard("Hall"));
        cards.add(new RoomCard("Lounge"));
        cards.add(new RoomCard("Dining Room"));
    }

    /**
     * Shuffles the deck randomly.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Returns the total number of cards in the deck.
     * @return number of cards
     */
    public int size() {
        return cards.size();
    }

    /**
     * Deals cards as evenly as possible to the given number of players.
     * Returns a list of hands, one per player.
     * @param numPlayers the number of players to deal to
     * @return list of card hands
     */
    public List<List<Card>> deal(int numPlayers) {
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            hands.add(new ArrayList<>());
        }
        for (int i = 0; i < cards.size(); i++) {
            hands.get(i % numPlayers).add(cards.get(i));
        }
        return hands;
    }

    /**
     * Returns all cards in the deck.
     * @return list of all cards
     */
    public List<Card> getCards() {
        return cards;
    }
}