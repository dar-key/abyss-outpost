package com.abyss.abyss_outpost.model;

public record Loot(String emoji, String name, int reward) implements Encounter {
    @Override
    public int danger() {
        return 0;
    }

    @Override
    public String kind() {
        return "loot";
    }
}
