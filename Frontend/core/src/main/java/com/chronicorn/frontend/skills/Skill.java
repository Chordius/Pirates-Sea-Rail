package com.chronicorn.frontend.skills;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.managers.battleManager.mechanics.ElementalEngine;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.statuseffect.StatusEffect;

public class Skill {
    private String id;
    private String name;
    private String iconId;
    private TargetScope scope; // 0 = None/Self, 1 = Single Enemy, 2 = All Enemies, 3 = Blast Enemies, etc.

    private int basePower;
    private int toughnessDMG = 20;
    private int energyBonus;
    private Elements element;
    private String tag;
    private String description;

    private Array<String> actionSequence;
    private SkillLogic mechanics;

    // Temp
    private boolean alwaysCrit = false;

    public Skill(String id, JsonValue data) {
        this.id = id;
        this.name = data.getString("name", "Unknown Skill");
        this.iconId = data.getString("iconId", "default_icon");

        this.scope = TargetScope.valueOf(data.getString("scope", "SINGLE_ENEMY"));
        this.element = Elements.valueOf(data.getString("element", "NONE"));

        this.basePower = data.getInt("basePower", 0);
        this.toughnessDMG = data.getInt("toughnessDMG", 20);
        this.energyBonus = data.getInt("energyBonus", 0);
        this.tag = data.getString("tag", "damage");
        this.description = data.getString("description", "Lorem Ipsum");

        // Load the action sequence array directly from JSON
        this.actionSequence = new Array<>();
        JsonValue seqArray = data.get("sequence");
        if (seqArray != null) {
            for (JsonValue val : seqArray) {
                this.actionSequence.add(val.asString());
            }
        }

        // Fetch the unique Java logic mapped to this ID
        this.mechanics = SkillLogicRegistry.getLogic(id);
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

    public int getToughnessDMG() {
        return toughnessDMG;
    }

    public String getDescription() { return description; }

    public Array<String> getActionSequence() {
        if (actionSequence == null) {
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
        return actionSequence;
    }

    public void setElement(Elements element) {
        this.element = element;
    }

    // The Concrete Command execution.
    // Subclasses like 'SonicSlash' will implement their own math here.
    public void apply(Battler user, Array<Battler> targets, Battler primaryTarget) {
        for (Battler target : targets) {
            int baseArgument = 0;

            mechanics.before(user, target, this);

            if ("damage".equals(tag)) {

                // 1. Calculate raw math (returns int locally to prevent thread/sharing bugs)
                int calculatedDamage = calcDMG(user, target);

                // 3. ATTACKER'S CONFIRM HOOKS (e.g., Attack up buffs)
                for (StatusEffect state : user.getActiveStates()) {
                    calculatedDamage = state.getLogic().onConfirm(user, target, state,this, calculatedDamage);
                }

                // 4. DEFENDER'S REACT HOOKS (e.g., Damage reduction shields)
                for (StatusEffect state : target.getActiveStates()) {
                    calculatedDamage = state.getLogic().onReact(user, target, this, calculatedDamage);
                }

                // 2. Process elemental impacts if applicable
                if (user instanceof Actor && target instanceof Enemy && this.element != Elements.NONE) {
                    calculatedDamage = ElementalEngine.processImpact(this, calculatedDamage, (Actor) user, (Enemy) target, primaryTarget == target);
                }

                baseArgument = Math.max(calculatedDamage, 0);
            }

            // TODO: Expand on this if needed

            // 3. Fire the specific Lambda logic for this skill
            mechanics.execute(user, target, baseArgument, this.element);

            for (StatusEffect state : target.getActiveStates()) {
                state.getLogic().onRespond(user, target, this, baseArgument);
            }
            for (StatusEffect state : user.getActiveStates()) {
                state.getLogic().onEstablish(user, target, state, this, baseArgument);
            }
        }

        // 4. After Logic
        energyHandling(user);
    }

    private int calcDMG(Battler user, Battler target) {
        int atk = user.getEffectivePrimaryParam(0);
        int def = target.getEffectivePrimaryParam(1);

        double rawDamage = (((double) (2 * user.getLevel()) / 5 + 2) * basePower * ((double) atk / def) * 0.02 + 2);

        // Fetch the 1.2x (Weak), 1.0x (Neutral), or 0.8x (Resist) modifier based on innate elements
        double elementRate = calcElement(target);

        // Count Determine Critical Modifier
        double critModifier = calcCrit(user);

        int finalDamage = (int) (rawDamage * elementRate * critModifier);
        return Math.max(finalDamage, 0); // Ensure it doesn't go below 0
    }

    private double calcElement(Battler target) {
        double elementRate = target.getElementalResistanceRate(this.element);

        if (elementRate >= 1.2) {
            System.out.println("It's super effective!");
        } else if (elementRate <= 0.8) {
            System.out.println("It's not very effective!");
        }
        return elementRate;
    }

    private double calcCrit(Battler user) {
        double critChance = user.getEffectiveSpParam(0);
        double critDamage = user.getEffectiveSpParam(1);
        double randomizer = Math.random() * 1;
        double finalDamage = 1;

        if (critChance <= randomizer || alwaysCrit) {
            finalDamage += critDamage;
        }

        if (alwaysCrit) {
            setAlwaysCrit(false);
        }

        return finalDamage;
    }


    public void energyHandling(Battler user) {
        user.energyChange(energyBonus);
    }

    public void setAlwaysCrit(boolean bool) {
        alwaysCrit = bool;
    }

    public void upgrade(int count) {

    };
}
