package com.chronicorn.frontend.managers.battleManager.enums;

public enum Elements {
    NONE, WATER, EARTH, LIGHTNING, FIRE, WIND;

    // Defines the 1.2x Weakness
    public Elements getWeakness() {
        switch (this) {
            case FIRE: return WATER;
            case EARTH: return FIRE;
            case LIGHTNING: return EARTH;
            case WATER: return LIGHTNING;
            default: return NONE;
        }
    }

    // Defines the 0.8x Resistance (The reverse of the weakness cycle)
    public Elements getResistance() {
        switch (this) {
            case WATER: return FIRE;
            case LIGHTNING: return WATER;
            case EARTH: return LIGHTNING;
            case FIRE: return EARTH;
            default: return NONE;
        }
    }
}
