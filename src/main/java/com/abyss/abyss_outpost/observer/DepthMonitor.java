package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;

public class DepthMonitor implements Subscriber {

    private final EventBus bus;
    private final int ratedDepth;
    private boolean alerted = false;

    public DepthMonitor(EventBus bus, int ratedDepth) {
        this.bus = bus;
        this.ratedDepth = ratedDepth;
    }

    public void onEvent(Event event) {
        if (!event.type().equals("DEPTH")) {
            return;
        }

        if (event.value() > ratedDepth && !alerted) {
            alerted = true;
            bus.publish(Event.log("danger",
                "Beyond rated depth (" + ratedDepth + " m)! The hull is taking crush damage."));
        }
    }
}
