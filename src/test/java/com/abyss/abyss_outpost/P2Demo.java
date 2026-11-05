
package com.abyss.abyss_outpost;

import com.abyss.abyss_outpost.model.Action;
import com.abyss.abyss_outpost.model.Event;
import com.abyss.abyss_outpost.observer.*;
import com.abyss.abyss_outpost.strategy.*;

public class P2Demo {

    public static void main(String[] args) {
        EventBus bus = new EventBus();
        LogSubscriber log = new LogSubscriber();

        bus.subscribe(log);
        bus.subscribe(new OxygenMonitor(bus, 100));
        bus.subscribe(new HullMonitor(bus, 100));
        bus.subscribe(new DepthMonitor(bus, 250));

        int[] oxygen = {80, 30, 25, 10};
        for (int value : oxygen) {
            bus.publish(Event.status("OXYGEN", value, ""));
        }

        int[] hull = {50, 40, 15};
        for (int value : hull) {
            bus.publish(Event.status("HULL", value, ""));
        }

        int[] depth = {245, 280, 315};
        for (int value : depth) {
            bus.publish(Event.status("DEPTH", value, ""));
        }

        var events = log.drain();
        System.out.println("-- first drain: " + events.size() + " alerts --");

        for (Event event : events) {
            System.out.println("[" + event.level() + "] " + event.message());
        }

        System.out.println("-- second drain: " + log.drain().size() + " events --");

        System.out.println("-- tactics --");

        DiveTactic[] tactics = {
            new CautiousTactic(),
            new GreedyTactic(),
            new StealthTactic()
        };

        for (DiveTactic tactic : tactics) {
            System.out.println(
                tactic.name() +
                ": oxygen " + tactic.oxygenPerTurn() +
                ", damage x" + tactic.damageMultiplier() +
                ", loot x" + tactic.lootMultiplier() +
                ", SNEAK " + tactic.successBonus(Action.SNEAK) +
                " / FIGHT " + tactic.successBonus(Action.FIGHT) +
                " / FLEE " + tactic.successBonus(Action.FLEE)
            );
        }
    }
}
