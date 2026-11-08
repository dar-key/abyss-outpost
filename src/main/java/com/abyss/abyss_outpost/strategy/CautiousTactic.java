
package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public class CautiousTactic implements DiveTactic {
    public String name() {
        return "Cautious";
    }

    public int oxygenPerTurn() {
        return 10;
    }

    @Override
    public int successBonus(Action action) {
        if (action == Action.SNEAK)
            return 10;
        if (action == Action.FIGHT)
            return -5;
        return 0;
    }

    public double damageMultiplier() {
        return 0.7;
    }

    public double lootMultiplier() {
        return 0.8;
    }
}
