package com.abyss.abyss_outpost.dto;

import java.util.List;

public record Report(String outcome, int score, int loot, int depth, int hullLeft, int oxygenLeft,
                     int turns, String submarine, String biome, String tactic,
                     List<String> creatures, List<String> items) { }