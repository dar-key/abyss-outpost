package com.abyss.abyss_outpost.model;

public record Creature(String emoji, String name, int danger, int reward) implements Encounter {
    @Override
    public String kind() {
        return "creature";
    }
}
