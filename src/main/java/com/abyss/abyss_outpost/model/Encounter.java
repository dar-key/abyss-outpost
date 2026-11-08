package com.abyss.abyss_outpost.model;

public interface Encounter {
    String emoji();
    String name();
    int danger(); 
    int reward();  
    String kind();  
}