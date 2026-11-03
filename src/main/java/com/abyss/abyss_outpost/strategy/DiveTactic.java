package com.abyss.abyss_outpost.strategy;

import com.abyss.abyss_outpost.model.Action;

public interface DiveTactic {
    String name();
    int oxygenPerTurn();             // как быстро тратится кислород за ход
    int successBonus(Action action); // +/- процент к шансу успеха действия
    double damageMultiplier();       // 1.0 = обычный получаемый урон
    double lootMultiplier();         // 1.0 = обычная добыча
}