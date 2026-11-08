package com.abyss.abyss_outpost.model;

public record Event(String type, String message, String level, int value) {
    public static Event log(String level, String message) {
        return new Event("LOG", message, level, 0);
    }

    public static Event status(String type, int value, String message) {
        return new Event(type, message, "info", value);
    }
}
