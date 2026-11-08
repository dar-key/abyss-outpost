package com.abyss.abyss_outpost.model;

import java.util.Objects;

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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public int getHull() {
        return hull;
    }

    @Override
    public int getOxygen() {
        return oxygen;
    }

    @Override
    public int getLight() {
        return light;
    }

    @Override
    public int getAttack() {
        return attack;
    }

    @Override
    public int getHeatResistance() {
        return heatResistance;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Submarine that))
            return false;
        return getMaxDepth() == that.getMaxDepth() &&
                getHull() == that.getHull() &&
                getOxygen() == that.getOxygen() &&
                getLight() == that.getLight() &&
                getAttack() == that.getAttack() &&
                getHeatResistance() == that.getHeatResistance() &&
                getCost() == that.getCost() &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, maxDepth, hull, oxygen, light, attack, heatResistance, cost);
    }

    @Override
    public String toString() {
        return "Submarine[" + description + ", depth=" + maxDepth + "m, hull=" + hull +
                ", o2=" + oxygen + ", light=" + light + ", atk=" + attack + ", cost=" + cost + "]";
    }
}
