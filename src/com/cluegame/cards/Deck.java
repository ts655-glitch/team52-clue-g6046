package com.cluegame.cards;
import com.cluegame.players.Player;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents the full 21-card deck. Handles shuffling, dealing and envelope population.
 * @author Team 52
 */
public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        populateDeck();
    }

    /** Populates deck with all 6 suspects, 6 weapons and 9 room cards. */
    private void populateDeck() {
        // Suspects
        cards.add(new SuspectCard("Col Mustard"));
        cards.add(new SuspectCard("Prof Plum"));
        cards.add(new SuspectCard("Rev Green"));
        cards.add(new SuspectCard("Mrs Peacock"));
        cards.add(new SuspectCard("Miss Scarlett"));
        cards.add(new SuspectCard("Mrs White"));
        // Weapons
        cards.add(new WeaponCard("Dagger"));
        cards.add(new WeaponCard("Candlestick"));
        cards.add(new WeaponCard("Revolver"));
        cards.add(new WeaponCard("Rope"));
        cards.add(new WeaponCard("Lead Piping"));
        cards.add(new WeaponCard("Spanner"));
        // Rooms
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
     * Shuffles and deals remaining cards to players after envelope is filled.
     * @param players list of players to deal to
     * @param envelope the murder envelope to populate first
     */
    public void shuffleAndDeal(List<Player> players, MurderEnvelope envelope) {
        Collections.shuffle(cards);
        // TODO: pick one of each type for envelope, deal rest round-robin to players
    }

    public List<Card> getCards() { return cards; }
}
