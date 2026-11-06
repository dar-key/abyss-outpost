package com.abyss.abyss_outpost.builder;

import com.abyss.abyss_outpost.model.BasicSubmarine;
import com.abyss.abyss_outpost.model.Submarine;

public class SubmarineBuilder {

    public enum Hull {
        STEEL("Steel hull", 60, 250, 100),
        TITANIUM("Titanium hull", 100, 450, 250);

        public final String label;
        public final int strength;
        public final int maxDepth;
        public final int cost;

        Hull(String label, int strength, int maxDepth, int cost) {
            this.label = label;
            this.strength = strength;
            this.maxDepth = maxDepth;
            this.cost = cost;
        }
    }

    public enum Engine {
        STANDARD("Standard engine", 100, 50),
        EFFICIENT("Efficient engine", 135, 150);

        public final String label;
        public final int oxygen;
        public final int cost;

        Engine(String label, int oxygen, int cost) {
            this.label = label;
            this.oxygen = oxygen;
            this.cost = cost;
        }
    }

    public enum Light {
        HEADLAMP("Headlamp", 1, 20),
        SEARCHLIGHT("Searchlight", 3, 80);

        public final String label;
        public final int power;
        public final int cost;

        Light(String label, int power, int cost) {
            this.label = label;
            this.power = power;
            this.cost = cost;
        }
    }

    public enum Tool {
        HARPOON("Harpoon", 2, 30),
        PLASMA_CUTTER("Plasma cutter", 4, 90);

        public final String label;
        public final int attack;
        public final int cost;

        Tool(String label, int attack, int cost) {
            this.label = label;
            this.attack = attack;
            this.cost = cost;
        }
    }

    private Hull hull = Hull.STEEL;
    private Engine engine = Engine.STANDARD;
    private Light light = Light.HEADLAMP;
    private Tool tool = Tool.HARPOON;

    public SubmarineBuilder hull(Hull hull) {
        this.hull = hull;
        return this;
    }

    public SubmarineBuilder engine(Engine engine) {
        this.engine = engine;
        return this;
    }

    public SubmarineBuilder light(Light light) {
        this.light = light;
        return this;
    }

    public SubmarineBuilder tool(Tool tool) {
        this.tool = tool;
        return this;
    }

    public Submarine build() {
        String description = hull.label + " · " + engine.label + " · " + light.label + " · " + tool.label;
        int maxDepth = hull.maxDepth;
        int hullPoints = hull.strength;
        int oxygen = engine.oxygen;
        int lightPower = light.power;
        int attack = tool.attack;
        int heatResistance = 0; 
        int totalCost = hull.cost + engine.cost + light.cost + tool.cost;

        return new BasicSubmarine(description, maxDepth, hullPoints, oxygen, lightPower, attack, heatResistance, totalCost);
    }
}
