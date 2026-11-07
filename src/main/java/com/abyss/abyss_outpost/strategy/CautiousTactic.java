
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class CautiousTactic implements DiveTactic {
    public String name() {
        return "Cautious";
    }

    public int oxygenPerTurn() {
        return 10;
    }

    public int successBonus(Action action) {
        return switch (action) {
            case SNEAK -> 10;
            case FIGHT -> -5;
            case FLEE -> 0;
        };
    }

    public double damageMultiplier() {
        return 0.7;
    }

    public double lootMultiplier() {
        return 0.8;
    }
}
