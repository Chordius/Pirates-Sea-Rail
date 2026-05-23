package com.chronicorn.frontend.items;

import com.chronicorn.frontend.battlers.Actor;
import java.util.Random;

public class Equippable extends Item {
    public enum StatType {
        ATTACK, DEFENSE, MAGIC, SPEED, MAX_HP,
        CRIT_RATE, CRIT_DMG, PENETRATION, BREAK_EFFECT, INCOMING_HEALING, RESISTANCE
    }

    private StatType fixedStat;
    private float fixedValue;

    private StatType randomStat;
    private float randomPercentBonus;

    private static final Random rand = new Random();

    public Equippable(String id, String name, String icon, String description, StatType fixedStat, float fixedValue, StatType randomStat) {
        super(id, name, icon, description, false);
        this.fixedStat = fixedStat;
        this.fixedValue = fixedValue;
        this.randomStat = randomStat;
        // Random value from 6% to 18% (0.06 to 0.18)
        this.randomPercentBonus = 0.06f + rand.nextFloat() * 0.12f;
    }

    public StatType getFixedStat() { return fixedStat; }
    public float getFixedValue() { return fixedValue; }

    public StatType getRandomStat() { return randomStat; }
    public float getRandomPercentBonus() { return randomPercentBonus; }

    @Override
    public boolean useOn(Actor target) {
        return target.equip(this);
    }
}
