package com.cluegame.model;
import java.util.Random;

/** Represents a pair of fair six-sided dice. @author Team 52 */
public class Dice {
    private Random random;
    public Dice() { this.random = new Random(); }

    /**
     * Rolls two fair six-sided dice and returns the total.
     * @return int between 2 and 12
     */
    public int roll() {
        return (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
    }
}