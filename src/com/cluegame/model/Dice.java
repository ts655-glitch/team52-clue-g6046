package com.cluegame.model;

import java.util.Random;

/**
 * Represents a pair of fair six-sided dice.
 * Each die has an equal probability of landing on any face (1-6).
 * @author Thanh Shaw
 */
public class Dice {

    private Random random;

    /**
     * Constructs a new Dice object with a random seed.
     */
    public Dice() {
        this.random = new Random();
    }

    /**
     * Rolls both dice and returns the total.
     * @return a random integer between 2 and 12 inclusive
     */
    public int roll() {
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        return die1 + die2;
    }

    /**
     * Rolls a single die and returns the result.
     * @return a random integer between 1 and 6 inclusive
     */
    public int rollSingle() {
        return random.nextInt(6) + 1;
    }
}