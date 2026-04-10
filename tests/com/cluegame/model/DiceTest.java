package com.cluegame.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Dice class.
 * @author Thanh Shaw
 */
public class DiceTest {

    /**
     * Rolls both dice 1000 times and checks the total is always between 2 and 12.
     * Covers requirement: two fair six-sided dice summed together.
     */
    @Test
    public void testRollIsInRange() {
        Dice dice = new Dice();
        for (int i = 0; i < 1000; i++) {
            int result = dice.roll();
            assertTrue(result >= 2 && result <= 12,
                "Expected roll between 2 and 12 but got " + result);
        }
    }

    /**
     * Rolls a single die 1000 times and checks the result is always between 1 and 6.
     * Covers requirement: a single die produces values 1 through 6 only.
     */
    @Test
    public void testRollSingleIsInRange() {
        Dice dice = new Dice();
        for (int i = 0; i < 1000; i++) {
            int result = dice.rollSingle();
            assertTrue(result >= 1 && result <= 6,
                "Expected single roll between 1 and 6 but got " + result);
        }
    }
}
