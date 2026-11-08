package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.builder.SubmarineBuilder;
import com.abyss.abyss_outpost.model.Submarine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicSubmarineTest {

    @Test
    @DisplayName("Identical submarines must be equal and share hash codes")
    void testEqualityAndHashCode() {
        Submarine sub1 = new SubmarineBuilder()
                .hull(SubmarineBuilder.Hull.STEEL)
                .engine(SubmarineBuilder.Engine.STANDARD)
                .build();

        Submarine sub2 = new SubmarineBuilder()
                .hull(SubmarineBuilder.Hull.STEEL)
                .engine(SubmarineBuilder.Engine.STANDARD)
                .build();

        assertEquals(sub1, sub2);
        assertEquals(sub1.hashCode(), sub2.hashCode());
    }

    @Test
    @DisplayName("Different configurations must not be equal")
    void testInequality() {
        Submarine steelSub = new SubmarineBuilder().hull(SubmarineBuilder.Hull.STEEL).build();
        Submarine titanSub = new SubmarineBuilder().hull(SubmarineBuilder.Hull.TITANIUM).build();

        assertNotEquals(steelSub, titanSub);
    }

    @Test
    @DisplayName("BasicSubmarine default sonar must be 0")
    void testDefaultSonar() {
        Submarine sub = new SubmarineBuilder().build();
        assertEquals(0, sub.getSonar());
    }

    @Test
    @DisplayName("ToString contains vital stats")
    void testToString() {
        Submarine sub = new SubmarineBuilder().build();
        String str = sub.toString();

        assertTrue(str.contains("depth=250m"));
        assertTrue(str.contains("cost=200"));
    }
}
