package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.dto.DockConfig;
import com.abyss.abyss_outpost.dto.SubSpec;
import com.abyss.abyss_outpost.facade.DiveSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiveSimulatorTest {

    private final DiveSimulator simulator = new DiveSimulator();

    @Test
    @DisplayName("Preview accepts valid budget configuration")
    void testValidSubmarinePreview() {
        DockConfig config = new DockConfig("STEEL", "STANDARD", "HEADLAMP", "HARPOON",
                List.of(), "SHALLOWS", "CAUTIOUS");

        SubSpec spec = simulator.preview(config);

        assertTrue(spec.affordable());
        assertEquals(200, spec.cost());
        assertEquals("", spec.warning());
    }

    @Test
    @DisplayName("Preview warns and rejects configuration over 600 budget")
    void testOverBudgetPreview() {
        // Titanium (250) + Efficient (150) + Searchlight (80) + Plasma cutter (90) +
        // Thermal (60) = 630
        DockConfig expensiveConfig = new DockConfig("TITANIUM", "EFFICIENT", "SEARCHLIGHT", "PLASMA_CUTTER",
                List.of("THERMAL"), "LAVA", "GREEDY");

        SubSpec spec = simulator.preview(expensiveConfig);

        assertFalse(spec.affordable());
        assertTrue(spec.warning().contains("Over budget"));
    }

    @Test
    @DisplayName("Start throws IllegalArgumentException if submarine exceeds budget")
    void testStartRejectsOverBudget() {
        DockConfig expensiveConfig = new DockConfig("TITANIUM", "EFFICIENT", "SEARCHLIGHT", "PLASMA_CUTTER",
                List.of("THERMAL"), "LAVA", "GREEDY");

        assertThrows(IllegalArgumentException.class, () -> simulator.start(expensiveConfig));
    }
}
