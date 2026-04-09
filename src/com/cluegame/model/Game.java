package com.cluegame.model;

import com.cluegame.cards.Card;
import com.cluegame.cards.Deck;
import com.cluegame.cards.MurderEnvelope;
import com.cluegame.cards.RoomCard;
import com.cluegame.cards.SuspectCard;
import com.cluegame.cards.WeaponCard;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level game controller. Manages turn order, game state and win condition.
 * @author Thanh Shaw
 */
public class Game {

    // starting squares for up to 6 players [row][col]
    private static final int[][] START_POSITIONS = {
        {0, 9}, {0, 14}, {23, 9}, {23, 14}, {9, 0}, {14, 0}
    };

    private Board board;
    private Deck deck;
    private MurderEnvelope murderEnvelope;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;
    private int turnCount;

    /**
     * Constructs a new Game with the given list of players.
     * @param players list of players (human and/or AI)
     */
    public Game(List<Player> players) {
        this.players = players;
        this.board = new Board();
        this.murderEnvelope = new MurderEnvelope();
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.turnCount = 0;
    }

    /**
     * Starts the game by shuffling the deck, sealing the murder envelope,
     * dealing remaining cards to players, and placing tokens on start squares.
     */
    public void startGame() {
        deck.shuffle();

        // pick one of each card type for the envelope and remove from deck
        SuspectCard envelopeSuspect = null;
        WeaponCard envelopeWeapon = null;
        RoomCard envelopeRoom = null;

        List<Card> allCards = deck.getCards();
        List<Card> toRemove = new ArrayList<>();

        for (Card card : allCards) {
            if (envelopeSuspect == null && card instanceof SuspectCard) {
                envelopeSuspect = (SuspectCard) card;
                toRemove.add(card);
            } else if (envelopeWeapon == null && card instanceof WeaponCard) {
                envelopeWeapon = (WeaponCard) card;
                toRemove.add(card);
            } else if (envelopeRoom == null && card instanceof RoomCard) {
                envelopeRoom = (RoomCard) card;
                toRemove.add(card);
            }
            if (envelopeSuspect != null && envelopeWeapon != null && envelopeRoom != null) {
                break;
            }
        }

        allCards.removeAll(toRemove);
        murderEnvelope.seal(envelopeSuspect, envelopeWeapon, envelopeRoom);

        // deal the remaining 18 cards to players
        List<List<Card>> hands = deck.deal(players.size());
        for (int i = 0; i < players.size(); i++) {
            for (Card card : hands.get(i)) {
                players.get(i).addCard(card);
            }
        }

        // place each player on their starting square
        for (int i = 0; i < players.size(); i++) {
            players.get(i).moveTo(START_POSITIONS[i][0], START_POSITIONS[i][1]);
        }
    }

    /**
     * Prints whose turn it is, increments the turn counter, then advances
     * currentPlayerIndex to the next active player.
     */
    public void nextTurn() {
        Player current = getCurrentPlayer();
        System.out.println("Turn " + (turnCount + 1) + ": " + current.getName() + "'s turn.");

        turnCount++;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());
    }

    /**
     * Checks whether an accusation matches the murder envelope.
     * @param accusation the accusation being made
     * @return true if accusation is correct
     */
    public boolean checkAccusation(Accusation accusation) {
        return murderEnvelope.verify(
            accusation.getSuspect(),
            accusation.getWeapon(),
            accusation.getRoom()
        );
    }

    /**
     * Quick test to verify dealing works - creates a 2 player game,
     * starts it and prints both hands.
     * @param args unused
     */
    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer("Alice", "Scarlett", 0, 0));
        players.add(new AIPlayer("Bob", "Mustard", 0, 0));

        Game game = new Game(players);
        game.startGame();

        System.out.println("Murder envelope: "
            + game.getMurderEnvelope().getSuspect().getName() + ", "
            + game.getMurderEnvelope().getWeapon().getName() + ", "
            + game.getMurderEnvelope().getRoom().getName());
        System.out.println();

        for (Player p : game.players) {
            System.out.println(p.getName() + " (row " + p.getRow() + ", col " + p.getCol() + "):");
            for (Card card : p.getHand()) {
                System.out.println("  " + card);
            }
        }
    }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public boolean isGameOver() { return gameOver; }
    public Board getBoard() { return board; }
    public MurderEnvelope getMurderEnvelope() { return murderEnvelope; }
    public int getTurnCount() { return turnCount; }
}
