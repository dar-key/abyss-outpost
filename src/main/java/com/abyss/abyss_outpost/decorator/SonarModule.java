package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public class SonarModule extends SubUpgrade {

    public SonarModule(Submarine inner) { super(inner); }

    @Override public String getDescription() { return inner.getDescription() + " + Sonar"; }
    @Override public int getSonar() { return inner.getSonar() + 1; }
    @Override public int getCost() { return inner.getCost() + 90; }
}