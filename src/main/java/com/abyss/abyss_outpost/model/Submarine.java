package com.abyss.abyss_outpost.model;

public interface Submarine {
    String getDescription();
    int getMaxDepth();        
    int getHull();            
    int getOxygen();          
    int getLight();           
    int getAttack();          
    int getHeatResistance();  
    int getCost();
    default int getSonar() { return 0; }
}