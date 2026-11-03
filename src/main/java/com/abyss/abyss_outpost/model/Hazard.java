package com.abyss.abyss_outpost.model;

public record Hazard(String emoji, String name, int danger, boolean heat) implements Encounter {
    @Override public int reward() { return 0; }
    @Override public String kind() { return "hazard"; }
}