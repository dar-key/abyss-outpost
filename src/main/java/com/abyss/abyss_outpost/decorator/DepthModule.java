package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public class DepthModule extends SubUpgrade {

    public DepthModule(Submarine inner) { super(inner); }

    @Override public String getDescription() { return inner.getDescription() + " + Depth module"; }
    @Override public int getMaxDepth() { return inner.getMaxDepth() + 200; }
    @Override public int getCost() { return inner.getCost() + 120; }
}