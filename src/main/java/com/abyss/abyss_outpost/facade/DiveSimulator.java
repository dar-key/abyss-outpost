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
    static final int ROUTE_COUNT = 3;
    static final int AIR_POCKET_OXYGEN = 25;
    static final int CURRENT_DEPTH = 40;
    static final String[] ROUTE_LABELS = {"Left", "Ahead", "Right"};
    private final Random rng;

    private Submarine sub;
    private BiomeFactory biome;
    private DiveTactic tactic;
    private EventBus bus;
    private LogSubscriber log;

    private int turn, depth, oxygen, oxygenMax, hull, hullMax, loot;
    private Encounter current;
    private final List<Encounter> routes = new ArrayList<>();
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
            warning = "Low oxygen for " + TOTAL_TURNS + " turns with the " + t.name() + " tactic. You will need air pockets.";
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
        routes.clear();
        current = null;

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
        offerRoutes();
    }

        /** Event odds: 40% creature, 25% hazard, 22% loot, 7% air pocket, 6% strong current. */
    private Encounter rollEncounter() {
        int r = rng.nextInt(100);
        if (r < 40) return biome.createCreature();
        if (r < 65) return biome.createHazard();
        if (r < 87) return biome.createLoot();
        if (r < 94) return new Phenomenon("", "Air pocket", Phenomenon.AIR_POCKET);
        return new Phenomenon("", "Strong current", Phenomenon.CURRENT);
    }

    private void offerRoutes() {
        routes.clear();
        current = null;
        for (int i = 0; i < ROUTE_COUNT; i++) routes.add(rollEncounter());
        if (sub.getSonar() > 0) {
            StringBuilder sb = new StringBuilder("Sonar: ");
            for (int i = 0; i < ROUTE_COUNT; i++) {
                sb.append(ROUTE_LABELS[i]).append(" ").append(routes.get(i).name());
                sb.append(i < ROUTE_COUNT - 1 ? ", " : ".");
            }
            bus.publish(Event.log("info", sb.toString()));
        } else {
            bus.publish(Event.log("info", "Three routes ahead. Without sonar you cannot tell what is on them."));
        }
    }

    public DiveState chooseRoute(RouteRequest req) {
        if (sub == null) throw new IllegalStateException("No dive in progress. Launch first.");
        if (over) throw new IllegalStateException("The dive is over.");
        if (routes.isEmpty()) throw new IllegalStateException("You already chose a route. Pick an action.");
        if (req.route() < 0 || req.route() >= routes.size()) {
            throw new IllegalArgumentException("Unknown route: " + req.route());
        }
        Encounter chosen = routes.get(req.route());
        routes.clear();
        if (chosen instanceof Phenomenon p) {
            resolvePhenomenon(p);  
            endTurn();
        } else {
            current = chosen;
            bus.publish(Event.log("warn", "Ahead: " + chosen.name() + "."));
        }
        return state();
    }

    private void resolvePhenomenon(Phenomenon p) {
        if (p.type().equals(Phenomenon.AIR_POCKET)) {
            int gained = Math.min(AIR_POCKET_OXYGEN, oxygenMax - oxygen);
            oxygen += gained;
            bus.publish(Event.status("OXYGEN", oxygen, ""));
            bus.publish(Event.log("good", "You find an air pocket. +" + gained + " oxygen."));
        } else {
            depth += CURRENT_DEPTH;
            oxygen += tactic.oxygenPerTurn();
            bus.publish(Event.status("DEPTH", depth, ""));
            bus.publish(Event.status("OXYGEN", oxygen, ""));
            bus.publish(Event.log("warn", "A strong current carries you " + CURRENT_DEPTH + " m deeper. It saves "
                    + tactic.oxygenPerTurn() + " oxygen."));
        }
    }

    private void endTurn() {
        if (checkVitals()) return;
        if (turn >= TOTAL_TURNS) finish("SURFACED", "You surface safely.");
        else advance();
    }

    private boolean checkVitals() {
        if (hull <= 0) { finish("HULL_BREACH", "The hull gives way. Dive over."); return true; }
        if (oxygen <= 0) { finish("OUT_OF_OXYGEN", "The oxygen runs out. Dive over."); return true; }
        return false;
    }

        public DiveState step(StepRequest req) {
        if (sub == null) throw new IllegalStateException("No dive in progress. Launch first.");
        if (over) throw new IllegalStateException("The dive is over.");
        if (current == null) throw new IllegalStateException("Choose a route first.");
        Action action = parse(Action.class, req.action());
        resolve(action);
        if (!over) endTurn();
        return state();
    }

    private void resolve(Action action) {
        Encounter e = current;

        if (action == Action.FLEE) {
            oxygen -= FLEE_OXYGEN_COST;
            bus.publish(Event.status("OXYGEN", oxygen, ""));
            bus.publish(Event.log("info", "You flee from " + e.name() + ". -" + FLEE_OXYGEN_COST + " oxygen."));
            checkVitals();
            return;
        }

        int base = action == Action.SNEAK ? 45 + sub.getLight() * 10 : 35 + sub.getAttack() * 10;
        int chance = base + tactic.successBonus(action) - e.danger() / 2;
        chance = Math.max(10, Math.min(95, chance));
        boolean success = rng.nextInt(100) < chance;

        if (e.kind().equals("creature")) creaturesMet.add(e.name());

        if (success) {
            double factor = (e.kind().equals("loot") || action == Action.FIGHT) ? 1.0 : 0.0;
            int gain = (int) Math.round(e.reward() * factor * tactic.lootMultiplier());
            if (gain > 0) {
                loot += gain;
                itemsCollected.add(e.name() + " (+" + gain + ")");
            }
            bus.publish(Event.log("good", successMessage(action, e, gain)));
        } else {
            double raw = e.danger() * (action == Action.FIGHT ? 1.5 : 1.0) * tactic.damageMultiplier();
            if (e instanceof Hazard h && h.heat()) {
                raw *= (100 - sub.getHeatResistance()) / 100.0;  
            }
            int damage = (int) Math.round(raw);
            hull -= damage;
            bus.publish(Event.status("HULL", hull, ""));
            bus.publish(Event.log("danger", e.name() + " gets the better of you: -" + damage + " hull."));
        }
        checkVitals();
    }

    private String successMessage(Action action, Encounter e, int gain) {
        if (e.kind().equals("loot")) return "You grab the " + e.name() + ". +" + gain;
        if (action == Action.FIGHT) return "You beat the " + e.name() + ". +" + gain;
        return "You slip past the " + e.name() + ".";
    }


    public DiveState state() {
        if (sub == null) throw new IllegalStateException("No dive in progress. Launch first.");
        return new DiveState(biome.name(), biome.id(), tactic.name(), turn, TOTAL_TURNS, depth,
                sub.getMaxDepth(), Math.max(oxygen, 0), oxygenMax, Math.max(hull, 0), hullMax, loot,
                EncounterView.of(current), routeViews(), over, outcome, log.drain());
    }

    private List<RouteView> routeViews() {
        List<RouteView> views = new ArrayList<>();
        boolean known = sub.getSonar() > 0;
        for (int i = 0; i < routes.size(); i++) {
            Encounter e = routes.get(i);
            views.add(known
                    ? new RouteView(i, ROUTE_LABELS[i], e.kind(), e.name(), e.danger(), e.reward(), true)
                    : new RouteView(i, ROUTE_LABELS[i], "unknown", "Unknown", 0, 0, false));
        }
        return views;
    }

    public Report report() {
        if (sub == null || !over) throw new IllegalStateException("The dive is not finished yet.");
        boolean survived = outcome.equals("SURFACED");
        int score = loot + depth / 10 + Math.max(hull, 0) / 2 + (survived ? 50 : 0);
        return new Report(outcome, score, loot, depth, Math.max(hull, 0), Math.max(oxygen, 0), turn,
                sub.getDescription(), biome.name(), tactic.name(),
                new ArrayList<>(creaturesMet), new ArrayList<>(itemsCollected));
    }

    private void finish(String result, String message) {
        over = true;
        outcome = result;
        current = null;
        bus.publish(Event.log("danger", message));
    }
}