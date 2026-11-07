package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private final List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(Event event) {
        for (Subscriber subscriber : new ArrayList<>(subscribers)) {
            subscriber.onEvent(event);
        }
    }
}
