package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.factory.BiomeFactory;
import com.abyss.abyss_outpost.factory.LavaFactory;
import com.abyss.abyss_outpost.factory.ShallowsFactory;
import com.abyss.abyss_outpost.model.Creature;
import com.abyss.abyss_outpost.model.Hazard;
import com.abyss.abyss_outpost.model.Loot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiomeFactoryTest {

    @Test
    @DisplayName("ShallowsFactory produces correct biome metadata and encounters")
    void testShallowsFactory() {
        BiomeFactory factory = new ShallowsFactory();

        assertEquals("SHALLOWS", factory.id());
        assertEquals("Sunlit Shallows", factory.name());
        assertEquals(35, factory.depthStep());

        Creature c = factory.createCreature();
        assertNotNull(c);
        assertEquals("creature", c.kind());
        assertTrue(c.danger() > 0);

        Hazard h = factory.createHazard();
        assertNotNull(h);
        assertEquals("hazard", h.kind());

        Loot l = factory.createLoot();
        assertNotNull(l);
        assertEquals("loot", l.kind());
        assertTrue(l.reward() > 0);
    }

    @Test
    @DisplayName("LavaFactory produces correct biome metadata and encounters")
    void testLavaFactory() {
        BiomeFactory factory = new LavaFactory();

        assertEquals("LAVA", factory.id());
        assertEquals("Magma Depths", factory.name());
        assertEquals(50, factory.depthStep());

        Creature c = factory.createCreature();
        assertNotNull(c);
        assertTrue(c.danger() >= 14);

        Hazard h = factory.createHazard();
        assertNotNull(h);

        Loot l = factory.createLoot();
        assertNotNull(l);
        assertTrue(l.reward() >= 25);
    }
}
