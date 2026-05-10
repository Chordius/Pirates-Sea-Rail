package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.BattleDelegate;
import com.chronicorn.frontend.observers.BattlerObserver;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillDatabase;
import com.chronicorn.frontend.statuseffect.StatusEffect;
import com.chronicorn.frontend.statuseffect.StatusEffectDatabase;

import java.util.HashMap;

public abstract class Battler {
    protected String name;

    // Derived Primary Parameters
    protected int hp;
    protected int maxHp;
    protected int energy;
    protected int maxEnergy;
    protected int attack;
    protected int defense;
    protected int magic;
    protected int speed;
    protected int level;

    // Primary Base Stats
    protected int baseMaxHp;
    protected int baseAttack;
    protected int baseDefense;
    protected int baseMagic;

    // SP Parameters
    protected float penetration = 1;
    protected float critrate = 0.05f;
    protected float critdmg = 0.5f;
    protected float breakeffect = 0f;
    protected float incominghealing = 0f;
    protected float resistance = 1;

    // Hidden Parameters
    protected float reactionDMGBonus = 0;

    // States
    protected Array<StatusEffect> activeStates = new Array<>();
    protected boolean isPlayerControlled;

    // Skills
    protected Array<Skill> skills = new Array<>();
    private Action currentAction;

    // Temps
    private HashMap<String, Object> tempVariables = new HashMap<>();

    // Observers & External Interrupts
    protected Array<BattlerObserver> observers = new Array<>();
    private BattleDelegate battleDelegate;

    public Battler(
        String name,
        int maxHp,
        int maxEnergy,
        int attack,
        int defense,
        int magic,
        int speed,
        int level,
        boolean isPlayerControlled
    ) {
        this.name = name;
        this.baseMaxHp = maxHp;
        this.hp = maxHp;
        this.maxEnergy = maxEnergy;
        this.energy = 0;
        this.baseAttack = attack;
        this.baseDefense = defense;
        this.baseMagic = magic;
        this.speed = speed;
        this.isPlayerControlled = isPlayerControlled;
        this.level = level;
        calculateParams(level);
    }

    // --- Core State Methods ---

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int amount, Elements element) {
        hp -= amount;
        if (hp < 0) hp = 0;
        notifyObserversOnHP(amount, element);
    }

    public void heal(int amount, Elements element) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
        notifyObserversOnHP(amount, element);
    }

    public void energyChange(int amount) {
        this.energy += amount;
        if (this.energy < 0) this.energy = 0;
        if (this.energy > maxEnergy) this.energy = maxEnergy;
        notifyObserversOnEN();
    }

    public void calculateParams(int level) {
        this.maxHp = Math.toIntExact(Math.round((double) ((2 * baseMaxHp * level) / 100 + level + 10) * 1.5));
        this.hp = maxHp;
        this.attack = Math.round((float) (2 * baseAttack * level) / 100) + 5;
        this.defense = Math.round((float) (2 * baseDefense * level) / 100) + 5;
        this.magic = Math.round((float) (2 * baseMagic * level) / 100) + 5;
    }


    // --- Getters ---

    public int getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getLevel() {
        return level;
    }

    public int getPrimaryParams(int number) {
        return switch (number) {
            case 0 -> attack;
            case 1 -> defense;
            case 2 -> magic;
            case 3 -> speed;
            default -> 0;
        };
    }

    public float getSpParams(int number) {
        return switch (number) {
            case 0 -> critrate;
            case 1 -> critdmg;
            case 2 -> penetration;
            case 3 -> breakeffect;
            case 4 -> incominghealing;
            case 5 -> resistance;
            default -> 0;
        };
    }

    public float getHiddenParam(int number) {
        return switch (number) {
            case 0 -> reactionDMGBonus;
            default -> 0;
        };
    }

    public float getEffectiveSpParam(int statId) {
        /**
         Intercepts a stat request and modifies it on the fly.
         @param owner The battler holding the state.
          * @param statId The ID of the stat being requested which are:
         *               0. CRIT Rate
         *               1. CRIT DMG
         *               2. Break Effect
         *               3. Incoming Healing
         *               4. Resistance
         * @param currentValue The value of the stat after previous states have modified it.
         * @return The newly modified stat.
         */
        // 1. Get the pure, untouched base stat
        float effectiveStat = getSpParams(statId);

        // 2. Push it through the pipeline of active states
        for (StatusEffect state : activeStates) {
            effectiveStat = state.getLogic().onModifySpParam(this, state.getOrigin(), statId, effectiveStat);
        }

        // 3. Return the final calculated number
        return effectiveStat;
    }

    public int getEffectivePrimaryParam(int statId) {
        /**
         * Intercepts a stat request and modifies it on the fly.
         * @param owner The battler holding the state.
         * @param statId The ID of the stat being requested which are:
         *               0. Attack
         *               1. Defense
         *               2. Magic
         *               3. Speed
         * @param currentValue The value of the stat after previous states have modified it.
         * @return The newly modified stat.
         */

        // 1. Get the pure, untouched base stat
        int effectiveStat = getPrimaryParams(statId);

        // 2. Push it through the pipeline of active states
        for (StatusEffect state : activeStates) {
            effectiveStat = state.getLogic().onModifyPrimaryParam(this, state.getOrigin(), statId, effectiveStat);
        }

        // 3. Return the final calculated number
        return effectiveStat;
    }

    public float getEffectiveHiddenParam(int statId) {
        /**
         * Intercepts a stat request and modifies it on the fly.
         * @param owner The battler holding the state.
         * @param statId The ID of the stat being requested which are:
         *               0. ReactionDMGBonus
         * @param currentValue The value of the stat after previous states have modified it.
         * @return The newly modified stat.
         */

        // 1. Get the pure, untouched base stat
        float effectiveStat = getHiddenParam(statId);

        // 2. Push it through the pipeline of active states
        for (StatusEffect state : activeStates) {
            effectiveStat = state.getLogic().onModifyHiddenParam(this, state.getOrigin(), statId, effectiveStat);
        }

        // 3. Return the final calculated number
        return effectiveStat;
    }

    public Array<StatusEffect> getActiveStates() {
        return activeStates;
    }

    public Array<Skill> getSkills() {
        return skills;
    }

    public double getElementalResistanceRate(Elements attackElement) {
        // Wind and None are neutral by default for raw damage multipliers
        if (attackElement == Elements.NONE || attackElement == Elements.WIND) {
            return 1.0;
        }

        double finalRate = 1.0;

        if (this instanceof Actor) {
            Elements innate = ((Actor) this).element;
            // Check for Weakness (1.2x)
            if (innate.getWeakness() == attackElement) {
                finalRate *= 1.2;
            }
            // Check for Resistance (0.8x)
            else if (innate.getResistance() == attackElement) {
                finalRate *= 0.8;
            }
        } else {
            for (Elements innate : ((Enemy) this).innateElements) {
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
        }

        // Apply the global RES stat modifier (e.g., a buff that lowers all elemental damage taken)
        finalRate *= 1 / getEffectiveSpParam(5);

        return finalRate;
    }

    public Object getTempVar(String key) {
        return tempVariables.get(key);
    }

    public boolean isPlayerControlled() {
        return isPlayerControlled;
    }

    // --- Setters ---
    public Action inputtingAction() {
        if (currentAction == null) {
            currentAction = new Action(this);
        }
        return currentAction;
    }

    public void clearAction() {
        currentAction = null;
    }

    public void setTempVar(String key, Object value) {
        tempVariables.put(key, value);
    }

    public void clearTempVars() {
        tempVariables.clear();
    }

    public void setPrimaryParams(int number, int value) {
        switch (number) {
            case 0 -> this.attack = value;
            case 1 -> this.defense = value;
            case 2 -> this.magic = value;
            case 3 -> this.speed = value;
            default -> {}
        };
    }

    public void setSpParams(int number, float value) {
         switch (number) {
            case 0 -> this.critrate = value;
            case 1 -> this.critdmg = value;
            case 2 -> this.penetration = value;
            case 3 -> this.breakeffect = value;
            case 4 -> this.incominghealing = value;
            case 5 -> this.resistance = value;
            default -> {}
         };
    }

    public void addStatusEffect(String status, Battler origin) {
        String finalString = status.toLowerCase();
        StatusEffect newEffect = StatusEffectDatabase.get(finalString);

        // 1. Guard clause for bad database fetches
        if (newEffect == null) {
            Gdx.app.error("Battler", "Attempted to get bad state: " + finalString);
            return;
        }

        // 2. Check for duplicates by comparing String IDs
        for (StatusEffect existingEffect : activeStates) {
            if (existingEffect.getId().equals(newEffect.getId())) {

                // Duplicate found: Refresh the duration instead of adding a second icon
                existingEffect.setTurns(newEffect.getTurns());
                System.out.println(this.name + "'s " + existingEffect.getName() + " was refreshed!");

                // Trigger UI update in case the turn indicator numbers changed
                notifyObserversOnStatus();
                return; // Exit the method completely
            }
        }

        newEffect.setOrigin(origin);

        // 3. If we reach this line, the state is completely new
        this.activeStates.add(newEffect);
        notifyObserversOnStatus();
        System.out.println(this.name + " obtained " + newEffect.getName());

        // Execute the hook using the existing reference
        newEffect.getLogic().onApply(this, newEffect);
    }

    public void addStatusEffect(String status) {
        addStatusEffect(status, this);
    }

    // Follow-up & Request Functions
    public void setBattleDelegate(BattleDelegate delegate) {
        this.battleDelegate = delegate;
    }

    public void requestFollowUp(String skillId, Battler target) {
        if (this.battleDelegate != null && this.isAlive()) {
            this.battleDelegate.onFollowUpRequested(this, skillId, target);
        }
    }

    public void requestReaction(String reactionSkillId, Battler centerTarget, int flatDamage) {
        // Even if the battler is dead (e.g. killed by thorns), reactions might still need to pop off
        if (this.battleDelegate != null) {
            this.battleDelegate.onReactionRequested(reactionSkillId, centerTarget, flatDamage);
        }
    }

    public void requestPopup(String text, Color color) {
        for (BattlerObserver observer : observers) {
            observer.onPopupRequested(text, color);
        }
    }

    // State Functions

    public void triggerActionStart() {
        for (int i = activeStates.size - 1; i >= 0; i--) {
            StatusEffect state = activeStates.get(i);
            state.getLogic().onActionStart(this);
        }
    }

    public void triggerTurnStart() {
        for (int i = activeStates.size - 1; i >= 0; i--) {
            StatusEffect state = activeStates.get(i);
            state.getLogic().onTurnStart(this);
        }
    }

    public void triggerActionEnd() {
        for (int i = activeStates.size - 1; i >= 0; i--) {
            StatusEffect state = activeStates.get(i);
            state.getLogic().onActionEnd(this);

            if (state.getTurnType().equals("action")) state.decrementTurn();

            // Remove the state if it has expired
            if (state.getTurns() <= 0) {
                System.out.println(this.getName() + "'s " + state.getId() + " wore off!");
                state.getLogic().onLeave(this, state);
                activeStates.removeIndex(i);
                notifyObserversOnStatus();
            }
        }
    }

    public void triggerTurnEnd() {
        for (int i = activeStates.size - 1; i >= 0; i--) {
            StatusEffect state = activeStates.get(i);
            state.getLogic().onTurnEnd(this, state.getOrigin());

            if (state.getTurnType().equals("turn")) state.decrementTurn();

            // Remove the state if it has expired
            if (state.getTurns() <= 0) {
                System.out.println(this.getName() + "'s " + state.getId() + " wore off!");
                state.getLogic().onLeave(this, state);
                activeStates.removeIndex(i);
                notifyObserversOnStatus();
            }
        }
    }

    // --- Observers ---
    public void addObserver(BattlerObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BattlerObserver observer) {
        observers.removeValue(observer, true);
    }

    protected void notifyObserversOnHP(int amount, Elements element) {
        for (BattlerObserver observer : observers) {
            observer.onHpChange(amount, element);
        }
    }

    protected void notifyObserversOnEN() {
        for (BattlerObserver observer : observers) {
            observer.onEnChange();
        }
    }

    protected void notifyObserversOnStatus() {
        for (BattlerObserver observer : observers) {
            observer.onStatsUpdated();
        }
    }

    // --- Abstract Methods ---

    /**
     * Called by the BattleManager during the INPUT state if the battler is AI-controlled.
     * The implementation should define how the enemy chooses a target and an action,
     * and then must call manager.submitAction(chosenAction).
     */
    public abstract void calculateAI(BattleManager manager);

    // Helper stuff
    // The new assignment method
    public void learnSkill(String skillId) {
        Skill skillOne = SkillDatabase.get(skillId);
        if (skillOne != null) {
            this.skills.add(skillOne);
        } else {
            Gdx.app.error("Battler", "Attempted to learn unknown skill: " + skillId);
        }
    }
}
