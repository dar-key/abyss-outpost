package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;

public class HullMonitor implements Subscriber {

    private final EventBus bus;
    private final int maxHull;
    private int lastLevel = 0;

    public HullMonitor(EventBus bus, int maxHull) {
        this.bus = bus;
        this.maxHull = maxHull;
    }

    public void onEvent(Event event) {
        if (!event.type().equals("HULL")) {
            return;
        }

        int percent = event.value() * 100 / maxHull;
        int level = 0;

        if (percent <= 20) {
            level = 2;
        } else if (percent <= 40) {
            level = 1;
        }

        if (level > lastLevel) {
            if (level == 2) {
                bus.publish(Event.log("danger", "HULL CRITICAL: " + percent + "%"));
            } else {
                bus.publish(Event.log("warn", "Hull integrity at " + percent + "%"));
            }
        }

        lastLevel = level;
    }
}
