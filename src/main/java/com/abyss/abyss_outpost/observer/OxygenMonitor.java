package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;

public class OxygenMonitor implements Subscriber {

    private final EventBus bus;
    private final int maxOxygen;
    private int lastLevel = 0;

    public OxygenMonitor(EventBus bus, int maxOxygen) {
        this.bus = bus;
        this.maxOxygen = maxOxygen;
    }

    public void onEvent(Event event) {
        if (!event.type().equals("OXYGEN")) {
            return;
        }

        int percent = event.value() * 100 / maxOxygen;
        int level = 0;

        if (percent <= 15) {
            level = 2;
        } else if (percent <= 30) {
            level = 1;
        }

        if (level > lastLevel) {
            if (level == 2) {
                bus.publish(Event.log("danger", "OXYGEN CRITICAL: " + percent + "%"));
            } else {
                bus.publish(Event.log("warn", "Oxygen low: " + percent + "%"));
            }
        }

        lastLevel = level;
    }
}
