package com.chronicorn.frontend.skills.sailor;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.Skill;

public class SonicSlash extends Skill {
    protected int upgradeCount;

    public SonicSlash() {
        super(
            "Sonic Slash",
            "slash",
            TargetScope.SINGLE,
            50,
            20,
            Elements.WIND
        );
    }

    @Override
    public void skill_logic(Battler user, Battler target) {
        // TODO: Create Skill Logic
        // Standard damage formula: User Attack - Target Defense
        target.takeDamage(damage);
        System.out.println(user.getName() + " uses Sonic Slash!");
        System.out.println(user.getName() + " dealt " + damage + " damage to " + target.getName() + "!");
    }

    @Override
    public void upgrade(int count) {
        // TODO: Create Upgrade Logic
        switch (upgradeCount) {
            case 1:
                basePower = 60;
            case 2:
                basePower = 70;
            default:
                basePower = 50;
        }
    }

}
