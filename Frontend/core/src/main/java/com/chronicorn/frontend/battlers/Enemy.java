package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.mechanics.ElementMark;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.Skill;

public class Enemy extends Battler {
    public static class EnemyDrop {
        public String itemId;
        public float chance;

        public EnemyDrop(String itemId, float chance) {
            this.itemId = itemId;
            this.chance = chance;
        }
    }

    private String id;
    protected int weaknessbar;
    protected int maxWeakness;
    protected Array<Elements> innateElements;
    protected boolean signature;
    protected ElementMark mark;
    private int baseExp = 50;
    private Array<EnemyDrop> drops = new Array<>();

    public int getBaseExp() {
        return baseExp;
    }

    public Array<EnemyDrop> getDrops() {
        return drops;
    }

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
        this.id = name.toLowerCase();
        this.maxWeakness = maxWeakness;
        this.weaknessbar = maxWeakness;
        this.signature = true;
        this.innateElements = new Array<>();
        this.mark = null;
        this.baseExp = 50;
    }

    public Enemy(String id, JsonValue data) {
        super(
            data.getString("name", "Unknown Enemy"),
            data.getInt("maxHp", 100),
            0,
            data.getInt("attack", 10),
            data.getInt("defense", 10),
            data.getInt("magic", 10),
            data.getInt("speed", 10),
            data.getInt("level", 1),
            false
        );
        this.id = id;
        this.maxWeakness = data.getInt("maxWeakness", 50);
        this.weaknessbar = this.maxWeakness;
        this.signature = true;
        this.innateElements = new Array<>();
        this.mark = null;
        this.baseExp = data.getInt("baseExp", 50);

        // Parse innate elements
        JsonValue elementsArray = data.get("innateElements");
        if (elementsArray != null) {
            for (JsonValue val : elementsArray) {
                this.innateElements.add(Elements.valueOf(val.asString().toUpperCase()));
            }
        }

        // Parse starting skills
        JsonValue skillsArray = data.get("skills");
        if (skillsArray != null) {
            for (JsonValue val : skillsArray) {
                this.learnSkill(val.asString());
            }
        }

        // Parse drops
        JsonValue dropsArray = data.get("drops");
        if (dropsArray != null) {
            for (JsonValue val : dropsArray) {
                String itemId = val.getString("itemId");
                float chance = val.getFloat("chance", 0f);
                this.drops.add(new EnemyDrop(itemId, chance));
            }
        }
    }

    public String getId() {
        return id;
    }

    public Array<Elements> getInnateElements() {
        return innateElements;
    }

    public int getWeaknessBar() {
        return weaknessbar;
    }

    @Override
    public void calculateAI(BattleManager manager) {
        EnemyAILogic logic = EnemyAIRegistry.getLogic(this.id);
        if (logic != null) {
            logic.execute(this, manager);
        } else {
            defaultAI(manager);
        }
    }

    private void defaultAI(BattleManager manager) {
        // 1. Gather all valid player targets
        Array<Battler> possibleTargets = new Array<>();
        for (Battler b : manager.getAllBattlers()) {
            if (b.isAlive() && b.isPlayerControlled()) {
                possibleTargets.add(b);
            }
        }

        // Safety check: Exit if no targets exist (prevents crashes)
        if (possibleTargets.size == 0) return;

        // 2. Select a random target
        Battler target = possibleTargets.random();

        // 3. Select a random skill from known moves
        Skill chosenSkill = this.getSkills().size > 0 ? this.getSkills().random() : null;
        if (chosenSkill == null) return;

        // 4. Fill the Action container
        Action action = this.inputtingAction();
        action.setSkill(chosenSkill);
        action.setPrimaryTarget(target);

        // 5. Submit to the manager and clear the container for the next turn
        manager.submitAction(action);
        this.clearAction();
    }

    public void setSignatureLoss(boolean b) {
        this.signature = !b;
        notifyObserversOnWeak();
    }

    public boolean isInSignatureLoss() {
        return !signature;
    }

    public void applyElementalMark(Elements attackElement, Actor source) {
        this.mark = new ElementMark(attackElement, source, 2);
        System.out.println("An element mark of " + this.mark + " has been applied!");
        notifyObserversOnStatus();
    }

    public void clearElementalMark() {
        this.mark = null;
        notifyObserversOnStatus();
    }

    public ElementMark getCurrentElementalMark() {
        return this.mark;
    }

    public void restoreWeakness() {
        this.weaknessbar = this.maxWeakness;
        this.signature = true;
        clearElementalMark();
        notifyObserversOnWeak();
    }

    public void reduceWeaknessBar(int i) {
        this.weaknessbar -= i;
        if (this.weaknessbar <= 0) {
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
