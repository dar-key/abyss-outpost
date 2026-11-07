package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.dto.DiveState;
import com.abyss.abyss_outpost.dto.DockConfig;
import com.abyss.abyss_outpost.dto.EncounterView;
import com.abyss.abyss_outpost.dto.Report;
import com.abyss.abyss_outpost.dto.RouteRequest;
import com.abyss.abyss_outpost.dto.RouteView;
import com.abyss.abyss_outpost.dto.StepRequest;
import com.abyss.abyss_outpost.facade.DiveSimulator;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class P3Benchmark {

    static int route(DiveState s) {
        List<RouteView> routes = s.routes();
        if (!routes.get(0).known()) return 0;
        int best = 0;
        double bestValue = -1e9;
        for (RouteView r : routes) {
            double value;
            switch (r.kind()) {
                case "loot" -> value = 100 + r.reward();
                case "event" -> value = r.name().equals("Air pocket")
                        ? (s.oxygen() * 100 / s.oxygenMax() < 60 ? 90 : 10) : 20;
                case "creature" -> value = 50 - r.danger();
                default -> value = 40 - r.danger();
            }
            if (value > bestValue) {
                bestValue = value;
                best = r.index();
            }
        }
        return best;
    }

    static String action(DiveState s) {
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
                if (s.encounter() == null) {
                    s = sim.chooseRoute(new RouteRequest(route(s)));
                } else {
                    s = sim.step(new StepRequest(action(s)));
                }
            }
            Report r = sim.report();
            endings.merge(r.outcome(), 1, Integer::sum);
            if (r.outcome().equals("SURFACED")) {
                survived++;
                scoreSum += r.score();
            }
        }
        System.out.printf("%-36s survived %5.1f%%, avg score %4.0f, %s%n",
                name, 100.0 * survived / dives, survived == 0 ? 0.0 : (double) scoreSum / survived, endings);
    }

    static DockConfig cfg(String hull, String engine, String light, String tool, List<String> up,
                          String biome, String tactic) {
        return new DockConfig(hull, engine, light, tool, up, biome, tactic);
    }

    public static void main(String[] args) {
        int n = 2000;
        run("A  balanced+Thermal, Shallows",
                cfg("STEEL", "EFFICIENT", "HEADLAMP", "HARPOON", List.of("THERMAL"), "SHALLOWS", "CAUTIOUS"), n);
        run("A+ same + Sonar",
                cfg("STEEL", "EFFICIENT", "HEADLAMP", "HARPOON", List.of("THERMAL", "SONAR"), "SHALLOWS", "CAUTIOUS"), n);
        run("B  cheapest sub, Lava",
                cfg("STEEL", "STANDARD", "HEADLAMP", "HARPOON", List.of(), "LAVA", "CAUTIOUS"), n);
        run("B+ same + Sonar",
                cfg("STEEL", "STANDARD", "HEADLAMP", "HARPOON", List.of("SONAR"), "LAVA", "CAUTIOUS"), n);
        run("C  cheapest sub, Shallows, Greedy",
                cfg("STEEL", "STANDARD", "HEADLAMP", "HARPOON", List.of(), "SHALLOWS", "GREEDY"), n);
        run("D  Titanium, Lava, Greedy",
                cfg("TITANIUM", "EFFICIENT", "HEADLAMP", "HARPOON", List.of(), "LAVA", "GREEDY"), n);
        run("D+ same + Sonar (cost 540)",
                cfg("TITANIUM", "EFFICIENT", "HEADLAMP", "HARPOON", List.of("SONAR"), "LAVA", "GREEDY"), n);

        DiveSimulator sim = new DiveSimulator();
        DockConfig tooExpensive = cfg("TITANIUM", "EFFICIENT", "SEARCHLIGHT", "PLASMA_CUTTER",
                List.of("THERMAL", "DEPTH", "SONAR"), "LAVA", "GREEDY");
        System.out.println("Over budget preview: " + sim.preview(tooExpensive).warning());
        try {
            sim.start(tooExpensive);
        } catch (IllegalArgumentException e) {
            System.out.println("Launch rejected: " + e.getMessage());
        }
    }
}