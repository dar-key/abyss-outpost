package com.abyss.abyss_outpost.factory;

import com.abyss.abyss_outpost.model.Creature;
import com.abyss.abyss_outpost.model.Hazard;
import com.abyss.abyss_outpost.model.Loot;

import java.util.List;

public class LavaFactory implements BiomeFactory {

    private static final List<Creature> CREATURES = List.of(
            new Creature("🦑", "Magma Squid", 16, 22),
            new Creature("🐍", "Ember Eel", 20, 28),
            new Creature("🦂", "Cinder Crab", 14, 20));

    private static final List<Hazard> HAZARDS = List.of(
            new Hazard("🌋", "Vent Eruption", 22, true),
            new Hazard("♨️", "Boiling Plume", 16, true),
            new Hazard("🪨", "Falling Basalt", 14, false));

    private static final List<Loot> LOOT = List.of(
            new Loot("💎", "Fire Crystal", 35),
            new Loot("🔶", "Obsidian Shard", 25),
            new Loot("🧪", "Thermal Ore", 30));

    @Override
    public String id() {
        return "LAVA";
    }

    @Override
    public String name() {
        return "Magma Depths";
    }

    @Override
    public int depthStep() {
        return 50;
    }

    @Override
    public Creature createCreature() {
        return Picker.pick(CREATURES);
    }

    @Override
    public Hazard createHazard() {
        return Picker.pick(HAZARDS);
    }

    @Override
    public Loot createLoot() {
        return Picker.pick(LOOT);
    }
}
