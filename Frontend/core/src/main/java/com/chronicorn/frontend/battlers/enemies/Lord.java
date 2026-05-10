package com.chronicorn.frontend.battlers.enemies;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.BattleManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.skills.Skill;

public class Lord extends Enemy {

    public Lord() {
        super(
            "Lord",
            35,
            15,
            15,
            15,
            18,
            1,
            50
        );

        this.innateElements.add(Elements.EARTH, Elements.FIRE);

        this.learnSkill("basic_attack");
    }


    @Override
    public void calculateAI(BattleManager manager) {
        // 1. Select a random alive player
        // 2. Choose an attack action
        // 3. Submit to the manager

        // Example (pseudo-code depending on your Action class):
        // Action attack = new AttackAction(this, target);
        // manager.submitAction(attack);

        // 1. Gather all valid player targets
        Array<Battler> possibleTargets = new Array<>();
        for (Battler b : manager.getAllBattlers()) {
            if (b.isAlive() && b.isPlayerControlled()) {
                possibleTargets.add(b);
            }
        }

        // Safety check: Exit if no targets exist (prevents crashes)
        if (possibleTargets.size == 0) return;

        // 2. Select a random target using LibGDX's built-in Array method
        Battler target = possibleTargets.random();

        // 3. Select a random skill from known moves
        Skill chosenSkill = this.getSkills().random();

        // 4. Fill the Action container
        Action action = this.inputtingAction();
        action.setSkill(chosenSkill);
        action.setPrimaryTarget(target);

        // 5. Submit to the manager and clear the container for the next turn
        manager.submitAction(action);
        this.clearAction();
    }
}
