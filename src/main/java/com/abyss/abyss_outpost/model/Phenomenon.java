package com.abyss.abyss_outpost.model;

public record Phenomenon(String emoji, String name, String type) implements Encounter {
    public static final String AIR_POCKET = "AIR_POCKET";
    public static final String CURRENT = "CURRENT";

    @Override public int danger() { return 0; }
    @Override public int reward() { return 0; }
    @Override public String kind() { return "event"; }
}