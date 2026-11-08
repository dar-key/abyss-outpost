package com.abyss.abyss_outpost.dto;

import java.util.List;

public record DockConfig(String hull, String engine, String light, String tool,
                         List<String> upgrades, String biome, String tactic) { }