package com.cluegame;

import com.cluegame.model.Game;
import com.cluegame.players.AIPlayer;
import com.cluegame.players.HumanPlayer;
import com.cluegame.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the Clue! console prototype.
 * Asks the user to configure the number of human and AI players,
 * then starts and runs the game.
 * @author Thanh Shaw
 */
public class Main {

    /**
     * Launches the game. Prompts for player setup then runs the game loop.
     * @param args unused
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("====================================");
        System.out.println("   CLUE! — Murder Mystery Game");
        System.out.println("   Console Prototype (Sprint 2)");
        System.out.println("====================================\n");

        // get number of human players
        System.out.println("How many human players? (0-5)");
        int numHumans = readInt(scanner, 0, 5);

        // get number of AI players (at least 1 AI, total 2-6)
        int minAI = Math.max(1, 2 - numHumans); // need at least 2 total
        int maxAI = 6 - numHumans;
        if (maxAI < minAI) {
            System.out.println("Too many human players — maximum 5 with at least 1 AI.");
            return;
        }
        System.out.println("How many AI players? (" + minAI + "-" + maxAI + ")");
        int numAI = readInt(scanner, minAI, maxAI);

        String[] characterNames = Game.getCharacterNames();
        String[] tokenNames = Game.getTokenNames();

        List<Player> players = new ArrayList<>();

        // create human players
        for (int i = 0; i < numHumans; i++) {
            System.out.println("\nHuman player " + (i + 1) + " will play as "
                    + characterNames[i] + ".");
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Player " + (i + 1);
            }
            players.add(new HumanPlayer(name, tokenNames[i], 0, 0, scanner));
        }

        // create AI players for the remaining slots
        for (int i = 0; i < numAI; i++) {
            int idx = numHumans + i;
            String aiName = "AI " + characterNames[idx];
            players.add(new AIPlayer(aiName, tokenNames[idx], 0, 0));
        }

        System.out.println("\n--- Player lineup ---");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String type = (p instanceof HumanPlayer) ? "Human" : "AI";
            System.out.println("  " + (i + 1) + ") " + p.getName()
                    + " as " + characterNames[i] + " [" + type + "]");
        }

        // create and run the game
        Game game = new Game(players);
        game.run();
    }

    /**
     * Reads an integer from the console within the given range (inclusive).
     * Keeps prompting until a valid number is entered.
     * @param scanner the Scanner for input
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return the valid integer entered
     */
    private static int readInt(Scanner scanner, int min, int max) {
        while (true) {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
