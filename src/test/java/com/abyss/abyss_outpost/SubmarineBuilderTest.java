package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.builder.SubmarineBuilder;
import com.abyss.abyss_outpost.model.Submarine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubmarineBuilderTest {

    @Test
    @DisplayName("Builder creates default submarine with standard configuration")
    void testDefaultSubmarine() {
        Submarine sub = new SubmarineBuilder().build();

        assertNotNull(sub);
        assertEquals(250, sub.getMaxDepth());
        assertEquals(60, sub.getHull());
        assertEquals(100, sub.getOxygen());
        assertEquals(1, sub.getLight());
        assertEquals(2, sub.getAttack());
        assertEquals(200, sub.getCost()); // 100 + 50 + 20 + 30
    }

    @Test
    @DisplayName("Builder accurately updates stats and computes total cost for high-end sub")
    void testCustomSubmarine() {
        Submarine sub = new SubmarineBuilder()
                .hull(SubmarineBuilder.Hull.TITANIUM)
                .engine(SubmarineBuilder.Engine.EFFICIENT)
                .light(SubmarineBuilder.Light.SEARCHLIGHT)
                .tool(SubmarineBuilder.Tool.PLASMA_CUTTER)
                .build();

        assertEquals(450, sub.getMaxDepth());
        assertEquals(100, sub.getHull());
        assertEquals(135, sub.getOxygen());
        assertEquals(3, sub.getLight());
        assertEquals(4, sub.getAttack());
        assertEquals(570, sub.getCost()); // 250 + 150 + 80 + 90
    }

    @Test
    @DisplayName("Builder rejects null arguments with NullPointerException")
    void testNullValidation() {
        SubmarineBuilder builder = new SubmarineBuilder();

        assertThrows(NullPointerException.class, () -> builder.hull(null));
        assertThrows(NullPointerException.class, () -> builder.engine(null));
        assertThrows(NullPointerException.class, () -> builder.light(null));
        assertThrows(NullPointerException.class, () -> builder.tool(null));
    }

    @Test
    @DisplayName("Reset method reverts builder back to default steel configuration")
    void testReset() {
        SubmarineBuilder builder = new SubmarineBuilder()
                .hull(SubmarineBuilder.Hull.TITANIUM)
                .engine(SubmarineBuilder.Engine.EFFICIENT);

        builder.reset();
        Submarine sub = builder.build();

        assertEquals(250, sub.getMaxDepth());
        assertEquals(100, sub.getOxygen());
    }
}
