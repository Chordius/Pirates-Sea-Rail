package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillDatabase;

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

    public void changeLevel(int level) {
        this.level = level;
        calculateParams(10);
    }

    @Override
    public void calculateAI(BattleManager manager) {

    }
}
