package com.abyss.abyss_outpost.factory;

import com.abyss.abyss_outpost.model.Creature;
import com.abyss.abyss_outpost.model.Hazard;
import com.abyss.abyss_outpost.model.Loot;

import java.util.List;

public class ShallowsFactory implements BiomeFactory {

    private static final List<Creature> CREATURES = List.of(
            new Creature("🐟", "Reef Piranha", 8, 10),
            new Creature("🦀", "Armored Crab", 10, 14),
            new Creature("🦈", "Reef Shark", 18, 25)
    );

    private static final List<Hazard> HAZARDS = List.of(
            new Hazard("🌊", "Strong Current", 8, false),
            new Hazard("🪸", "Sharp Coral", 10, false),
            new Hazard("🕸️", "Abandoned Net", 6, false)
    );

    private static final List<Loot> LOOT = List.of(
            new Loot("🐚", "Pearl Shell", 15),
            new Loot("⚙️", "Wreck Scrap", 10),
            new Loot("📦", "Supply Crate", 20)
    );

    @Override
    public String id() {
        return "SHALLOWS";
    }

    @Override
    public String name() {
        return "Sunlit Shallows";
    }

    @Override
    public int depthStep() {
        return 35;
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
