package com.abyss.abyss_outpost.dto;

public record SubSpec(String description, int hull, int maxDepth, int oxygen, int light,
                      int attack, int heatResistance, int cost, int budget,
                      boolean affordable, String warning) { }