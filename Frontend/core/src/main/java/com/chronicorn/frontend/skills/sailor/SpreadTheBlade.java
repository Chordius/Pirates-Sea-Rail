package com.chronicorn.frontend.skills.sailor;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.Skill;

public class SpreadTheBlade extends Skill {
    protected int basePower;
    protected int upgradeCount;

    public SpreadTheBlade() {
        super(
            "Spread the Blade",
            "gale",
            TargetScope.BLAST,
            70,
            30,
            Elements.WIND
        );
    }

    @Override
    public void skill_logic(Battler user, Battler target) {
        // TODO: Create Skill Logic
        target.takeDamage(damage);
        System.out.println(user.getName() + " uses Spread the Blade!");
        System.out.println(user.getName() + " dealt " + damage + " damage to " + target.getName() + "!");
    }

    @Override
    public void upgrade(int count) {
        // TODO: Create Upgrade Logic
        switch (upgradeCount) {
            case 1:
                basePower = 80;
            case 2:
                basePower = 90;
            default:
                basePower = 70;
        }
    }

}
