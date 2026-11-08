
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class GreedyTactic implements DiveTactic {

    public String name() {
        return "Greedy";
    }

    public int oxygenPerTurn() {
        return 13;
    }

    @Override
    public int successBonus(Action action) {
        if (action == Action.SNEAK)
            return -10;
        if (action == Action.FIGHT)
            return 15;
        return 0;
    }

    public double damageMultiplier() {
        return 1.3;
    }

    public double lootMultiplier() {
        return 1.6;
    }
}
