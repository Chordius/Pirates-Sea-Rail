package com.chronicorn.frontend.skills;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.managers.battleManager.mechanics.TargetingLogic;

import java.util.ArrayList;

public class Action {
    private Battler user;
    private Skill skill;
    private Array<Battler> targets;
    private Battler primaryTarget;

    public void setPrimaryTarget(Battler target) { this.primaryTarget = target; }

    public Battler getPrimaryTarget() { return primaryTarget; }

    public Action(Battler user) {
        this.user = user;
        this.targets = new Array<>();
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public void setTarget(Battler target) {
        this.targets.clear();
        this.targets.add(target);
    }

    public void setTargets(Array<Battler> targets) {
        this.targets.clear();
        this.targets.addAll(targets);
    }

    public Skill getSkill() {
        return skill;
    }

    public boolean needsSelection() {
        if (skill == null || skill.getScope() == null) return false;
        return skill.getScope() != TargetScope.SELF;
    }

    public void resolveTargets(ArrayList<Battler> allBattlers) {
        if (skill == null || skill.getScope() == null) return;

        Array<Battler> resolved = TargetingLogic.resolveTargets(user, primaryTarget, targets, allBattlers, skill.getScope());
        targets.clear();
        targets.addAll(resolved);

        if (primaryTarget == null && targets.size > 0) {
            primaryTarget = targets.first();
        }
    }

    // Called by your BattleManager during the EXECUTE_ACTION state
    public void execute() {
        if (skill != null) {
            // TODO: Shuffle primary target to be index 0.
            if (primaryTarget != null) {
                int index = targets.indexOf(primaryTarget, true);

                if (index != -1) {
                    // Remove from current position and move to front
                    targets.removeIndex(index);
                    targets.insert(0, primaryTarget);
                } else if (targets.size == 0) {
                    // Fallback: If targets is empty for some reason, add the primary
                    targets.add(primaryTarget);
                }
            }
            skill.apply(user, targets, primaryTarget);
        }
    }
}
