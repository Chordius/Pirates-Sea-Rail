package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.mechanics.ElementMark;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;

public class Enemy extends Battler {
    protected int weaknessbar;
    protected int maxWeakness;
    protected Array<Elements> innateElements = new Array<>();
    protected boolean signature;
    protected ElementMark mark;

    public Enemy(
        String name,
        int maxHp,
        int attack,
        int defense,
        int magic,
        int speed,
        int level,
        int maxWeakness
    ) {
        // name, maxHp, speed, isPlayerControlled
        super(name, maxHp, 0, attack, defense, magic, speed, level, false);
        this.maxWeakness = maxWeakness;
        this.weaknessbar = maxWeakness;
        this.signature = true;
        this.innateElements = new Array<>();
        this.mark = null;
    }

    public Array<Elements> getInnateElements() {
        return innateElements;
    }

    public int getWeaknessBar() {
        return weaknessbar;
    }

    @Override
    public void calculateAI(BattleManager manager) {
        // 1. Select a random alive player
        // 2. Choose an attack action
        // 3. Submit to the manager

        // Example (pseudo-code depending on your Action class):
        // Action attack = new AttackAction(this, target);
        // manager.submitAction(attack);
    }

    public void setSignatureLoss(boolean b) {
        this.signature = !b;
    }

    public boolean isInSignatureLoss() {
        return !signature;
    }

    public void applyElementalMark(Elements attackElement, Actor source) {
        this.mark = new ElementMark(attackElement, source, 2);
        System.out.println("An element mark of " + this.mark + " has been applied!");
    }

    public void clearElementalMark() {
        this.mark = null;
    }

    public ElementMark getCurrentElementalMark() {
        return this.mark;
    }

    public double getElementalResistanceRate(Elements attackElement) {
        // Wind and None are neutral by default for raw damage multipliers
        if (attackElement == Elements.NONE || attackElement == Elements.WIND) {
            return 1.0;
        }

        double finalRate = 1.0;

        for (Elements innate : this.innateElements) {
            // Check for Weakness (1.2x)
            if (innate.getWeakness() == attackElement) {
                finalRate *= 1.2;
            }
            // Check for Resistance (0.8x)
            else if (innate.getResistance() == attackElement) {
                finalRate *= 0.8;
            }
            // If neither, it implicitly remains 1.0x
        }

        // Apply the global RES stat modifier (e.g., a buff that lowers all elemental damage taken)
        finalRate *= resistance;

        return finalRate;
    }

    public void reduceWeaknessBar(int i) {
        this.weaknessbar -= i;
        if (this.weaknessbar < 0) {
            this.weaknessbar = 0;
        }
        notifyObserversOnWeak();
    }

    public int getMaxWeakness() {
        return maxWeakness;
    }

    protected void notifyObserversOnWeak() {
        for (BattlerObserver observer : observers) {
            observer.onWeakChange();
        }
    }
}
