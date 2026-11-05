package com.abyss.abyss_outpost.facade;

import com.abyss.abyss_outpost.builder.SubmarineBuilder;
import com.abyss.abyss_outpost.decorator.Upgrades;
import com.abyss.abyss_outpost.dto.*;
import com.abyss.abyss_outpost.factory.*;
import com.abyss.abyss_outpost.model.*;
import com.abyss.abyss_outpost.observer.*;
import com.abyss.abyss_outpost.strategy.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DiveSimulator {

    static final int TOTAL_TURNS = 8;
    static final int BUDGET = 600;
    static final int FLEE_OXYGEN_COST = 8;
    static final int CRUSH_DAMAGE = 15;

    private final Random rng;

    private Submarine sub;
    private BiomeFactory biome;
    private DiveTactic tactic;
    private EventBus bus;
    private LogSubscriber log;

    private int turn, depth, oxygen, oxygenMax, hull, hullMax, loot;
    private Encounter current;
    private boolean over;
    private String outcome = "DIVING";
    private final List<String> creaturesMet = new ArrayList<>();
    private final List<String> itemsCollected = new ArrayList<>();

    public DiveSimulator() { this(new Random()); }
    public DiveSimulator(Random rng) { this.rng = rng; }

    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, String.valueOf(value).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName().toLowerCase() + ": " + value);
        }
    }

    private Submarine buildSub(DockConfig c) {
        Submarine s = new SubmarineBuilder()
                .hull(parse(SubmarineBuilder.Hull.class, c.hull()))
                .engine(parse(SubmarineBuilder.Engine.class, c.engine()))
                .light(parse(SubmarineBuilder.Light.class, c.light()))
                .tool(parse(SubmarineBuilder.Tool.class, c.tool()))
                .build();
        if (c.upgrades() != null) {
            for (String id : c.upgrades()) s = Upgrades.apply(s, id);
        }
        return s;
    }

    private BiomeFactory pickBiome(String id) {
        return switch (String.valueOf(id).toUpperCase()) {
            case "SHALLOWS" -> new ShallowsFactory();
            case "LAVA" -> new LavaFactory();
            default -> throw new IllegalArgumentException("Unknown biome: " + id);
        };
    }

    private DiveTactic pickTactic(String id) {
        return switch (String.valueOf(id).toUpperCase()) {
            case "CAUTIOUS" -> new CautiousTactic();
            case "GREEDY" -> new GreedyTactic();
            case "STEALTH" -> new StealthTactic();
            default -> throw new IllegalArgumentException("Unknown tactic: " + id);
        };
    }

    public SubSpec preview(DockConfig c) {
        Submarine s = buildSub(c);
        DiveTactic t = pickTactic(c.tactic());
        pickBiome(c.biome());  
        String warning = "";
        if (s.getOxygen() < t.oxygenPerTurn() * TOTAL_TURNS) {
            warning = "Not enough oxygen for " + TOTAL_TURNS + " turns with the " + t.name() + " tactic.";
        }
        boolean affordable = s.getCost() <= BUDGET;
        if (!affordable) warning = "Over budget: " + s.getCost() + " / " + BUDGET + ".";
        return new SubSpec(s.getDescription(), s.getHull(), s.getMaxDepth(), s.getOxygen(), s.getLight(),
                s.getAttack(), s.getHeatResistance(), s.getCost(), BUDGET, affordable, warning);
    }

    public DiveState start(DockConfig c) {
        Submarine s = buildSub(c);
        if (s.getCost() > BUDGET) {
            throw new IllegalArgumentException("Over budget: " + s.getCost() + " / " + BUDGET + ".");
        }
        sub = s;
        biome = pickBiome(c.biome());
        tactic = pickTactic(c.tactic());

        bus = new EventBus();
        log = new LogSubscriber();
        bus.subscribe(log);
        bus.subscribe(new OxygenMonitor(bus, sub.getOxygen()));
        bus.subscribe(new HullMonitor(bus, sub.getHull()));
        bus.subscribe(new DepthMonitor(bus, sub.getMaxDepth()));

        turn = 0; depth = 0; loot = 0;
        oxygen = oxygenMax = sub.getOxygen();
        hull = hullMax = sub.getHull();
        over = false;
        outcome = "DIVING";
        creaturesMet.clear();
        itemsCollected.clear();

        bus.publish(Event.log("info", "Launching into the " + biome.name() + " with the " + tactic.name() + " tactic."));
        advance();
        return state();
    }

    private void advance() {
        turn++;
        depth += biome.depthStep();
        oxygen -= tactic.oxygenPerTurn();
        bus.publish(Event.status("DEPTH", depth, ""));
        bus.publish(Event.status("OXYGEN", oxygen, ""));
        bus.publish(Event.log("info", "Turn " + turn + "/" + TOTAL_TURNS + ": depth " + depth + " m."));
        if (depth > sub.getMaxDepth()) {
            hull -= CRUSH_DAMAGE;
            bus.publish(Event.log("danger", "Crush damage: -" + CRUSH_DAMAGE + " hull."));
        }
        bus.publish(Event.status("HULL", hull, ""));
        if (checkVitals()) return;
        nextEncounter();
    }

    private void nextEncounter() {
        int r = rng.nextInt(100);
        if (r < 45) current = biome.createCreature();
        else if (r < 75) current = biome.createHazard();
        else current = biome.createLoot();
        bus.publish(Event.log("warn", "Ahead: " + current.name() + "."));
    }

    /** Returns true if the dive just ended. Hull is checked first. */
    private boolean checkVitals() {
        if (hull <= 0) { finish("HULL_BREACH", "The hull gives way. Dive over."); return true; }
        if (oxygen <= 0) { finish("OUT_OF_OXYGEN", "The oxygen runs out. Dive over."); return true; }
        return false;
    }

    private void finish(String result, String message) {
        over = true;
        outcome = result;
        current = null;
        bus.publish(Event.log("danger", message));
    }
}