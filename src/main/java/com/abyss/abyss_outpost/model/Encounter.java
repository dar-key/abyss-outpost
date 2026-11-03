package com.abyss.abyss_outpost.model;

public interface Encounter {
    String emoji();
    String name();
    int danger();   // типа дамага если профукаем
    int reward();   // награда если преуспеем
    String kind();  // "creature", "hazard" или "loot"
}