package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.dto.DiveState;
import com.abyss.abyss_outpost.dto.DockConfig;
import com.abyss.abyss_outpost.dto.EncounterView;
import com.abyss.abyss_outpost.dto.Report;
import com.abyss.abyss_outpost.dto.StepRequest;
import com.abyss.abyss_outpost.facade.DiveSimulator;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class P3Benchmark {

    static String policy(DiveState s) {
        EncounterView e = s.encounter();
        if (e.kind().equals("loot")) return "FIGHT";
        if (s.hull() < 30 && e.danger() >= 14) return "FLEE";
        return "SNEAK";
    }

    static void run(String name, DockConfig config, int dives) {
        DiveSimulator sim = new DiveSimulator(new Random(42));
        int survived = 0;
        long scoreSum = 0;
        Map<String, Integer> endings = new TreeMap<>();
        for (int i = 0; i < dives; i++) {
            DiveState s = sim.start(config);
            while (!s.over()) {
                s = sim.step(new StepRequest(policy(s)));
            }
            Report r = sim.report();
            endings.merge(r.outcome(), 1, Integer::sum);
            if (r.outcome().equals("SURFACED")) {
                survived++;
                scoreSum += r.score();
            }
        }
        System.out.printf("%s: survived %.1f%%, average score %.0f, endings %s%n",
                name, 100.0 * survived / dives, survived == 0 ? 0.0 : (double) scoreSum / survived, endings);
    }

    public static void main(String[] args) {
        run("A balanced + Thermal, Shallows, Cautious ",
                new DockConfig("STEEL", "EFFICIENT", "HEADLAMP", "HARPOON", List.of("THERMAL"), "SHALLOWS", "CAUTIOUS"), 1000);
        run("B cheapest sub, Lava, Cautious           ",
                new DockConfig("STEEL", "STANDARD", "HEADLAMP", "HARPOON", List.of(), "LAVA", "CAUTIOUS"), 1000);
        run("C cheapest sub, Shallows, Greedy         ",
                new DockConfig("STEEL", "STANDARD", "HEADLAMP", "HARPOON", List.of(), "SHALLOWS", "GREEDY"), 1000);
        run("D Titanium + Efficient, Lava, Greedy     ",
                new DockConfig("TITANIUM", "EFFICIENT", "HEADLAMP", "HARPOON", List.of(), "LAVA", "GREEDY"), 1000);

        DiveSimulator sim = new DiveSimulator();
        DockConfig tooExpensive = new DockConfig("TITANIUM", "EFFICIENT", "SEARCHLIGHT", "PLASMA_CUTTER",
                List.of("THERMAL", "DEPTH"), "LAVA", "GREEDY");
        System.out.println("Over budget preview: " + sim.preview(tooExpensive).warning());
        try {
            sim.start(tooExpensive);
        } catch (IllegalArgumentException e) {
            System.out.println("Launch rejected: " + e.getMessage());
        }
    }
}