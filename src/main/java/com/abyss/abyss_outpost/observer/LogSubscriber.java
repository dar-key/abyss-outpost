package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;
import java.util.ArrayList;
import java.util.List;

public class LogSubscriber implements Subscriber {

    private final List<Event> events = new ArrayList<>();

    public void onEvent(Event event) {
        if (event.message() != null && !event.message().isBlank()) {
            events.add(event);
        }
    }

    public List<Event> drain() {
        List<Event> result = new ArrayList<>(events);
        events.clear();
        return result;
    }
}
