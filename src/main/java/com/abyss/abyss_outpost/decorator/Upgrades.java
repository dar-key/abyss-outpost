package com.abyss.abyss_outpost.decorator;

import com.abyss.abyss_outpost.model.Submarine;

public final class Upgrades {

    private Upgrades() { }

    public static Submarine apply(Submarine sub, String id) {
        return switch (id) {
            case "THERMAL" -> new ThermalPlating(sub);
            case "FLOOD" -> new Floodlight(sub);
            case "DEPTH" -> new DepthModule(sub);
            case "SONAR" -> new SonarModule(sub);
            default -> throw new IllegalArgumentException("Unknown upgrade: " + id);
        };
    }
}