
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class GreedyTactic implements DiveTactic {

    public String name() {
        return "Greedy";
    }

    public int oxygenPerTurn() {
        return 13;
    }

    public int successBonus(Action action) {
        return switch (action) {
            case SNEAK -> -10;
            case FIGHT -> 15;
            case FLEE -> 0;
        };
    }

    public double damageMultiplier() {
        return 1.3;
    }

    public double lootMultiplier() {
        return 1.6;
    }
}
