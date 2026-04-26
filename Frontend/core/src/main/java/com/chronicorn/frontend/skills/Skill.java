package com.chronicorn.frontend.skills;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.managers.battleManager.mechanics.ElementalEngine;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public abstract class Skill {
    protected String name;
    protected String iconId;
    protected TargetScope scope; // 0 = None/Self, 1 = Single Enemy, 2 = All Enemies, 3 = Blast Enemies, etc.

    protected int basePower;
    protected int toughnessDMG = 20;
    protected int damage;
    protected int energyBonus;
    protected Elements element;

    public Skill(String name, String iconId, TargetScope scope, Elements element) {
        this.name = name;
        this.iconId = iconId;
        this.scope = scope;
        this.element = element;
        this.basePower = 0;
        this.energyBonus = 0;
    }

    public Skill(String name, String iconId, TargetScope scope, int basePower, int energy, Elements element) {
        this.name = name;
        this.iconId = iconId;
        this.scope = scope;
        this.basePower = basePower;
        this.energyBonus = energy;
        this.element = element;
    }

    public String getName() {
        return name;
    }

    public String getIconId() {
        return iconId;
    }

    public TargetScope getScope() {
        return scope;
    }

    public Elements getElement() {
        return element;
    }

    public int getDamage() {
        return damage;
    }

    public int getToughnessDMG() {
        return toughnessDMG;
    }

    // The Concrete Command execution.
    // Subclasses like 'SonicSlash' will implement their own math here.
    public void apply(Battler user, Array<Battler> targets, Battler primaryTarget) {
        for (Battler target : targets) {
            calcDMG(user, target);

            if (user instanceof Actor && target instanceof Enemy) {
                if (this.element != Elements.NONE) {
                    this.damage = ElementalEngine.processImpact(this, this.damage, (Actor) user, (Enemy) target, primaryTarget == target);
                }
            }

            // Core Logic
            skill_logic(user, target);
        }

        // After Logic
        energyHandling(user);
    };

    // public abstract
    public abstract void skill_logic(Battler user, Battler target);

    public void calcDMG(Battler user, Battler target) {
        int atk = user.getPrimaryParams(0);
        int def = target.getPrimaryParams(1);

        double rawDamage = (((double) (2 * user.getLevel()) / 5 + 2) * basePower * ((double) atk / def) * 0.02 + 2);

        // Fetch the 1.2x (Weak), 1.0x (Neutral), or 0.8x (Resist) modifier based on innate elements
        double elementRate = target.getElementalResistanceRate(this.element);

        if (elementRate >= 1.2) {
            System.out.println("It's super effective!");
        }
        if (elementRate <= 0.8) {
            System.out.println("It's not very effective!");
        }

        this.damage = (int) (rawDamage * elementRate);

        if (this.damage < 0) {
            this.damage = 0;
        }
    }

    public void energyHandling(Battler user) {
        user.energyChange(energyBonus);
    }

    public Array<String> getActionSequence() {
        Array<String> sequence = new Array<>();
        sequence.add("PERFORM START");
        sequence.add("WAIT: 0.2");
        sequence.add("ANIMATION: hit");
        sequence.add("ANIMATION: punch-fire");
        sequence.add("ANIMATION: hit-fire");
        sequence.add("WAIT: 0.1"); // Wait a fraction of a second so damage pops up right as the sword connects
        sequence.add("ACTION EFFECT"); // MATH HAPPENS HERE
        sequence.add("WAIT FOR ANIMATION");
        sequence.add("WAIT: 0.2");
        return sequence;
    }

    public abstract void upgrade(int count);
}
