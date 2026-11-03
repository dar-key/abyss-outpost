package com.abyss.abyss_outpost.model;

public class BasicSubmarine implements Submarine {
    private final String description;
    private final int maxDepth, hull, oxygen, light, attack, heatResistance, cost;

    public BasicSubmarine(String description, int maxDepth, int hull, int oxygen,
                          int light, int attack, int heatResistance, int cost) {
        this.description = description;
        this.maxDepth = maxDepth;
        this.hull = hull;
        this.oxygen = oxygen;
        this.light = light;
        this.attack = attack;
        this.heatResistance = heatResistance;
        this.cost = cost;
    }

    @Override public String getDescription() { return description; }
    @Override public int getMaxDepth() { return maxDepth; }
    @Override public int getHull() { return hull; }
    @Override public int getOxygen() { return oxygen; }
    @Override public int getLight() { return light; }
    @Override public int getAttack() { return attack; }
    @Override public int getHeatResistance() { return heatResistance; }
    @Override public int getCost() { return cost; }
}