
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class StealthTactic implements DiveTactic {

    public String name() {
        return "Stealth";
    }

    public int oxygenPerTurn() {
        return 11;
    }

    @Override
    public int successBonus(Action action) {
        if (action == Action.SNEAK)
            return 25;
        if (action == Action.FIGHT)
            return -15;
        return 0;
    }

    public double damageMultiplier() {
        return 1.0;
    }

    public double lootMultiplier() {
        return 1.0;
    }
}
