package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public interface DiveTactic {
    String name();
    int oxygenPerTurn();             
    int successBonus(Action action); 
    double damageMultiplier();       
    double lootMultiplier();         
}