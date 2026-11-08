package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public abstract class SubUpgrade implements Submarine {
    protected final Submarine inner;

    protected SubUpgrade(Submarine inner) { this.inner = inner; }

    @Override public String getDescription() { return inner.getDescription(); }
    @Override public int getMaxDepth() { return inner.getMaxDepth(); }
    @Override public int getHull() { return inner.getHull(); }
    @Override public int getOxygen() { return inner.getOxygen(); }
    @Override public int getLight() { return inner.getLight(); }
    @Override public int getAttack() { return inner.getAttack(); }
    @Override public int getHeatResistance() { return inner.getHeatResistance(); }
    @Override public int getCost() { return inner.getCost(); }
    @Override public int getSonar() { return inner.getSonar(); }
}