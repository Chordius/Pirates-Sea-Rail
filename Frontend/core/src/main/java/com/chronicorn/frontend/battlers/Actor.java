package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillDatabase;

import com.chronicorn.frontend.items.Equippable;

public class Actor extends Battler {
    protected String id;
    protected int shield = 0;
    protected Elements element;
    protected int portraitOffsetX;
    protected int portraitOffsetY;

    protected Array<Equippable> equipments = new Array<Equippable>(2);

    public Actor(
            String name,
            int maxHp,
            int maxEnergy,
            int attack,
            int defense,
            int magic,
            int speed) {
        // name, maxHp, speed, isPlayerControlled
        super(name, maxHp, maxEnergy, attack, defense, magic, speed, 5, true);
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

    public String getId() {
        return id;
    }

    public void setId(String string) {
        this.id = string;
    }

    private int exp = 0;

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExpNeededForNextLevel() {
        return level * level * level * 2;
    }

    public void gainExp(int amount) {
        this.exp += amount;
        while (this.exp >= getExpNeededForNextLevel()) {
            this.exp -= getExpNeededForNextLevel();
            this.level++;
            calculateParams(this.level);
        }
    }

    public void changeLevel(int level) {
        this.level = level;
        calculateParams(this.level);
    }

    public boolean equip(Equippable item) {
        if (equipments.size < 2) {
            equipments.add(item);
            calculateParams(this.level);
            return true;
        }
        return false;
    }

    public void unequip(Equippable item) {
        if (equipments.removeValue(item, true)) {
            calculateParams(this.level);
        }
    }

    public Array<Equippable> getEquipments() {
        return equipments;
    }

    @Override
    public void calculateParams(int level) {
        super.calculateParams(level);

        // Reset speed and SP Parameters to default before applying equipment bonuses
        this.speed = this.baseSpeed;
        this.penetration = 1f;
        this.critrate = 0.05f;
        this.critdmg = 0.5f;
        this.breakeffect = 0f;
        this.incominghealing = 0f;
        this.resistance = 1f;

        // Base param accumulators for multiplier calculation (only base stats affected
        // by % bonus?)
        // Wait, the prompt said "Each equippables can modify the Battler Actor's
        // Derived Primary Parameters (or SP Parameters). ... random stat with random
        // values from 6% to 18% bonus values."
        // We will apply fixed bonuses first, then percentage bonuses.
        float bonusAttackPrc = 0f;
        float bonusDefensePrc = 0f;
        float bonusMagicPrc = 0f;
        float bonusSpeedPrc = 0f;
        float bonusHpPrc = 0f;

        if (equipments != null) {
            for (Equippable eq : equipments) {
                // Apply fixed stat
                applyFixedStat(eq.getFixedStat(), eq.getFixedValue());
                // Collect random stat percentage bonus
                applyRandomStatBonus(eq.getRandomStat(), eq.getRandomPercentBonus());
            }
        }
    }

    private void applyFixedStat(Equippable.StatType type, float value) {
        if (type == null)
            return;
        switch (type) {
            case ATTACK:
                this.attack += value;
                break;
            case DEFENSE:
                this.defense += value;
                break;
            case MAGIC:
                this.magic += value;
                break;
            case SPEED:
                this.speed += value;
                break;
            case MAX_HP:
                this.maxHp += value;
                this.hp = Math.min(this.hp + (int) value, this.maxHp);
                break;
            case CRIT_RATE:
                this.critrate += value;
                break;
            case CRIT_DMG:
                this.critdmg += value;
                break;
            case PENETRATION:
                this.penetration += value;
                break;
            case BREAK_EFFECT:
                this.breakeffect += value;
                break;
            case INCOMING_HEALING:
                this.incominghealing += value;
                break;
            case RESISTANCE:
                this.resistance += value;
                break;
        }
    }

    private void applyRandomStatBonus(Equippable.StatType type, float bonusPrc) {
        if (type == null)
            return;
        switch (type) {
            case ATTACK:
                this.attack += this.attack * bonusPrc;
                break;
            case DEFENSE:
                this.defense += this.defense * bonusPrc;
                break;
            case MAGIC:
                this.magic += this.magic * bonusPrc;
                break;
            case SPEED:
                this.speed += this.speed * bonusPrc;
                break;
            case MAX_HP:
                int hpDiff = (int) (this.maxHp * bonusPrc);
                this.maxHp += hpDiff;
                this.hp = Math.min(this.hp + hpDiff, this.maxHp);
                break;
            case CRIT_RATE:
                this.critrate += bonusPrc;
                break; // For SP params, % bonus just adds linearly usually? Or maybe normal percent?
                       // The prompt: "random values from 6% to 18% bonus values." We'll just add it
                       // flatly for SP, and multiply for primary params.
            case CRIT_DMG:
                this.critdmg += bonusPrc;
                break;
            case PENETRATION:
                this.penetration += bonusPrc;
                break;
            case BREAK_EFFECT:
                this.breakeffect += bonusPrc;
                break;
            case INCOMING_HEALING:
                this.incominghealing += bonusPrc;
                break;
            case RESISTANCE:
                this.resistance += bonusPrc;
                break;
        }
    }

    @Override
    public void calculateAI(BattleManager manager) {

    }
}
