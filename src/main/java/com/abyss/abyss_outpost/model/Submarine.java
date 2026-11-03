package com.abyss.abyss_outpost.model;

public interface Submarine {
    String getDescription();
    int getMaxDepth();        // сколько метров прежде чем начнёт действовать урон от сдавливания
    int getHull();            // точки корпуса
    int getOxygen();          // это запас кислорода
    int getLight();           // помогает при стелсе
    int getAttack();          // помогает в бою
    int getHeatResistance();  // % меньше урона от опасных источников теплаб типа вулканы и т.д.
    int getCost();
}