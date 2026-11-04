package com.abyss.abyss_outpost.facade;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DiveSimulator {

    // Игрок выбирает корпус, двигатель и т.д. Позже подключим с Builder + Decorator
    public Map<String, Object> preview(Map<String, Object> config) {
        return Map.of("status", "TODO preview");
    }

    // Начало погружения. Позже подключим BiomeFactory + DiveTactic + EventBus
    public Map<String, Object> start(Map<String, Object> config) {
        return Map.of("status", "TODO start");
    }

    // Один ход для выбранного действия (SNEAK, FIGHT, FLEE)
    public Map<String, Object> step(String action) {
        return Map.of("status", "TODO step", "action", String.valueOf(action));
    }

    // Итоги после конца погружения
    public Map<String, Object> report() {
        return Map.of("status", "TODO report");
    }
}