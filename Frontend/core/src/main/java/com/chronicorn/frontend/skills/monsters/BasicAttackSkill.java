package com.chronicorn.frontend.skills.monsters;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.Skill;

public class BasicAttackSkill extends Skill {

    public BasicAttackSkill(int basePower) {
        // Name, Icon ID, Scope (1 = Single Target)
        super("Attack", "attack_icon", TargetScope.SINGLE, basePower, 0, Elements.NONE);
    }

    @Override
    public void skill_logic(Battler user, Battler target) {
        target.takeDamage(damage);
        System.out.println(user.getName() + " dealt " + damage + " damage to " + target.getName() + "!");
    }

    @Override
    public void upgrade(int count) {

    }
}
