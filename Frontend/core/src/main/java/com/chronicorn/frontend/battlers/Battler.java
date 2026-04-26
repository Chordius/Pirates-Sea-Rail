package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.states.battleStates.BattleState;

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

    // States
    protected Array<BattleState> states;
    protected boolean isPlayerControlled;

    // Skills
    protected Array<Skill> skills = new Array<>();
    private Action currentAction;

    // Observers
    protected Array<BattlerObserver> observers = new Array<>();

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

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
        notifyObserversOnHP();
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
        notifyObserversOnHP();
    }

    public void energyChange(int amount) {
        this.energy += amount;
        if (this.energy < 0) this.energy = 0;
        if (this.energy > maxEnergy) this.energy = maxEnergy;
        notifyObserversOnEN();
    }

    public void calculateParams(int level) {
        this.maxHp = (int) ((int) (double) ((2 * 70 * level) / 100 + level + 10) * 1.5);
        this.hp = maxHp;
        this.attack = ((2 * baseAttack * level) / 100) + 5;
        this.defense = ((2 * baseDefense * level) / 100) + 5;
        this.magic =((2 * baseMagic * level) / 100) + 5;
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
            default -> 0;
        };
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

    public Array<Skill> getSkills() {
        return skills;
    }

    public void addObserver(BattlerObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BattlerObserver observer) {
        observers.removeValue(observer, true);
    }

    protected void notifyObserversOnHP() {
        for (BattlerObserver observer : observers) {
            observer.onHpChange();
        }
    }

    protected void notifyObserversOnEN() {
        for (BattlerObserver observer : observers) {
            observer.onEnChange();
        }
    }

    public abstract double getElementalResistanceRate(Elements attackElement);

    // --- Abstract Methods ---

    /**
     * Called by the BattleManager during the INPUT state if the battler is AI-controlled.
     * The implementation should define how the enemy chooses a target and an action,
     * and then must call manager.submitAction(chosenAction).
     */
    public abstract void calculateAI(BattleManager manager);

    public void addStatusEffect(String status) {

    }
}
