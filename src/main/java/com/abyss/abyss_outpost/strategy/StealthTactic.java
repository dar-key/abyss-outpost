
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class StealthTactic implements DiveTactic {

    public String name() {
        return "Stealth";
    }

    public int oxygenPerTurn() {
        return 11;
    }

    public int successBonus(Action action) {
        return switch (action) {
            case SNEAK -> 25;
            case FIGHT -> -15;
            case FLEE -> 0;
        };
    }

    public double damageMultiplier() {
        return 1.0;
    }

    public double lootMultiplier() {
        return 1.0;
    }
}
