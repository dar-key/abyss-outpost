package com.abyss.abyss_outpost.dto;

import com.abyss.abyss_outpost.model.Encounter;

public record EncounterView(String kind, String emoji, String name, int danger, int reward) {

    public static EncounterView of(Encounter e) {
        if (e == null) {
            return null;
        }
        return new EncounterView(e.kind(), e.emoji(), e.name(), e.danger(), e.reward());
    }
}