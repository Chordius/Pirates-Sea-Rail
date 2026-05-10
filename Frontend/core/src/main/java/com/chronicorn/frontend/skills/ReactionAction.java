package com.chronicorn.frontend.skills;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.mechanics.TargetingLogic;

import java.util.ArrayList;

public class ReactionAction extends Action {
    private final int precalculatedDamage;
    private String reactionName;
    private Color reactionColor;

    // Constructor takes a NULL owner, the target, and the exact damage to deal
    public ReactionAction(Skill reactionSkill, Battler target, int flatDamage) {
        super(null); // Unowned!
        this.setSkill(reactionSkill);
        this.setPrimaryTarget(target);
        String reactionName = getSkill().getName();
        Elements element = getSkill().getElement();
        setReactionParams(reactionName, element);
        this.precalculatedDamage = flatDamage;
    }

    public void setReactionParams(String string, Elements element) {
        reactionName = string;
        reactionColor = element.getElementColor();
        System.out.println(reactionColor.toString());
    }

    // Override the execute method triggered by "ACTION EFFECT" in the sequence
    @Override
    public void execute() {
        // Bypass the SkillLogic and calcDMG() entirely.
        // Just apply the flat damage to the targets.
        for (Battler target : getTargets()) {
            if (target.isAlive()) {
                System.out.println(target.getName() + "'s HP Before: " + target.getHp());
                target.takeDamage(precalculatedDamage, this.getSkill().getElement());
                target.requestPopup(reactionName + "!", reactionColor);
                System.out.println(target.getName() + "'s HP After: " + target.getHp());
            }
        }
    }
}
