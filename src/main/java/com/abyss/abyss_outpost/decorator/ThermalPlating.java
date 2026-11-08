package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public class ThermalPlating extends SubUpgrade {

    public ThermalPlating(Submarine inner) { super(inner); }

    @Override public String getDescription() { return inner.getDescription() + " + Thermal plating"; }
    @Override public int getHull() { return inner.getHull() + 40; }
    @Override public int getHeatResistance() { return inner.getHeatResistance() + 50; }
    @Override public int getCost() { return inner.getCost() + 60; }
}