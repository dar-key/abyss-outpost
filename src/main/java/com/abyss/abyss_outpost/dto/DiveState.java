package com.abyss.abyss_outpost.dto;

import com.abyss.abyss_outpost.model.Event;

import java.util.List;

public record DiveState(String biome, String biomeId, String tactic, int turn, int totalTurns,
                        int depth, int maxDepth, int oxygen, int oxygenMax, int hull, int hullMax,
                        int loot, EncounterView encounter, List<RouteView> routes, boolean over,
                        String outcome, List<Event> events) { }