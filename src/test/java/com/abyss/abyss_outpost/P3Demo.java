package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.decorator.Upgrades;
import com.abyss.abyss_outpost.model.BasicSubmarine;
import com.abyss.abyss_outpost.model.Submarine;

public class P3Demo {
    public static void main(String[] args) {
        Submarine base = new BasicSubmarine("Test sub", 250, 60, 100, 1, 2, 0, 200);
        print("Base", base);

        Submarine upgraded = Upgrades.apply(Upgrades.apply(base, "THERMAL"), "DEPTH");
        print("Thermal + Depth", upgraded);

        Submarine all = Upgrades.apply(upgraded, "FLOOD");
        print("+ Floodlight", all);

        try {
            Upgrades.apply(base, "TURBO");
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown id rejected: " + e.getMessage());
        }
    }

    private static void print(String title, Submarine s) {
        System.out.println(title + ": " + s.getDescription());
        System.out.println("  hull=" + s.getHull() + " maxDepth=" + s.getMaxDepth() + " oxygen=" + s.getOxygen()
                + " light=" + s.getLight() + " attack=" + s.getAttack()
                + " heat=" + s.getHeatResistance() + " cost=" + s.getCost());
    }
}