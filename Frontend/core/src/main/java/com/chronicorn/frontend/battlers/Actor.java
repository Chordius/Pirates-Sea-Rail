package com.chronicorn.frontend.battlers;

import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class Actor extends Battler {
    protected int shield = 0;
    protected Elements element;
    protected int portraitOffsetX;
    protected int portraitOffsetY;

    public Actor(
        String name,
        int maxHp,
        int maxEnergy,
        int attack,
        int defense,
        int magic,
        int speed
    ) {
        // name, maxHp, speed, isPlayerControlled
        super(name, maxHp, maxEnergy, attack, defense, magic, speed, 1, true);
    }

    public int getShield() {
        return shield;
    }

    public Elements getElement() {
        return element;
    }

    public int getPortraitOffsetX() {
        return portraitOffsetX;
    }

    public int getPortraitOffsetY() {
        return portraitOffsetY;
    }

    public double getElementalResistanceRate(Elements attackElement) {
        // Wind and None are neutral by default for raw damage multipliers
        if (attackElement == Elements.NONE || attackElement == Elements.WIND) {
            return 1.0;
        }

        double finalRate = 1.0;

        Elements innate = this.element;
        // Check for Weakness (1.2x)
        if (innate.getWeakness() == attackElement) {
            finalRate *= 1.2;
        }
        // Check for Resistance (0.8x)
        else if (innate.getResistance() == attackElement) {
            finalRate *= 0.8;
        }
        // If neither, it implicitly remains 1.0x

        // Apply the global RES stat modifier (e.g., a buff that lowers all elemental damage taken)
        finalRate *= resistance;

        return finalRate;
    }

    public void changeLevel(int level) {
        this.level = level;
    }

    @Override
    public void calculateAI(BattleManager manager) {

    }
}
