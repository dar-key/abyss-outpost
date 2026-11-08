package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.builder.SubmarineBuilder;
import com.abyss.abyss_outpost.factory.BiomeFactory;
import com.abyss.abyss_outpost.factory.LavaFactory;
import com.abyss.abyss_outpost.factory.ShallowsFactory;
import com.abyss.abyss_outpost.model.Submarine;

import java.util.List;

public class P1Demo {

    public static void main(String[] args) {
        Submarine defaultSub = new SubmarineBuilder().build();
        System.out.println("Default sub: " + defaultSub.getDescription());
        System.out.printf("hull=%d depth=%d oxygen=%d light=%d attack=%d cost=%d%n",
                defaultSub.getHull(), defaultSub.getMaxDepth(), defaultSub.getOxygen(),
                defaultSub.getLight(), defaultSub.getAttack(), defaultSub.getCost());

        Submarine customSub = new SubmarineBuilder()
                .hull(SubmarineBuilder.Hull.TITANIUM)
                .engine(SubmarineBuilder.Engine.EFFICIENT)
                .light(SubmarineBuilder.Light.SEARCHLIGHT)
                .tool(SubmarineBuilder.Tool.PLASMA_CUTTER)
                .build();
        System.out.println("Custom sub: " + customSub.getDescription());
        System.out.printf("hull=%d depth=%d oxygen=%d light=%d attack=%d cost=%d%n",
                customSub.getHull(), customSub.getMaxDepth(), customSub.getOxygen(),
                customSub.getLight(), customSub.getAttack(), customSub.getCost());

        List<BiomeFactory> factories = List.of(new ShallowsFactory(), new LavaFactory());
        for (BiomeFactory factory : factories) {
            System.out.printf("%n== %s (%d m/turn) ==%n", factory.name(), factory.depthStep());
            for (int i = 0; i < 4; i++) {
                System.out.println(factory.createCreature());
                System.out.println(factory.createHazard());
                System.out.println(factory.createLoot());
            }
        }
    }
}
