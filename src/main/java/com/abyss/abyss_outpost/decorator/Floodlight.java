package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public class Floodlight extends SubUpgrade {

    public Floodlight(Submarine inner) { super(inner); }

    @Override public String getDescription() { return inner.getDescription() + " + Floodlight"; }
    @Override public int getLight() { return inner.getLight() + 2; }
    @Override public int getCost() { return inner.getCost() + 40; }
}