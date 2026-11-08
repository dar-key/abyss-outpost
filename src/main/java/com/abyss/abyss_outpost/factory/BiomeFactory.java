package com.abyss.abyss_outpost.factory;

import com.abyss.abyss_outpost.model.Creature;
import com.abyss.abyss_outpost.model.Hazard;
import com.abyss.abyss_outpost.model.Loot;

public interface BiomeFactory {
    String id();

    String name();

    int depthStep();

    Creature createCreature();

    Hazard createHazard();

    Loot createLoot();
}
