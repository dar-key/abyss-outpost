package com.abyss.abyss_outpost.observer;

import com.abyss.abyss_outpost.model.Event;

public interface Subscriber {
    void onEvent(Event event);
}