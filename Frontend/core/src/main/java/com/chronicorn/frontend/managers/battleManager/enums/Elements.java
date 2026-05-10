package com.chronicorn.frontend.managers.battleManager.enums;

import com.badlogic.gdx.graphics.Color;

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

    public Color getElementColor() {
        switch (this) {
            case WIND:
                return Color.valueOf("82cd98");
            case FIRE:
                return Color.valueOf("dc5151");
            case WATER:
                return Color.valueOf("82abcd");
            case LIGHTNING:
                return Color.valueOf("aa61b7");
            case EARTH:
                return Color.TAN;
            default:
                return Color.WHITE;
        }
    }
}
