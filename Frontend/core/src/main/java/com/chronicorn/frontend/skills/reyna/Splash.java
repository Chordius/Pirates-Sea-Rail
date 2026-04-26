package com.chronicorn.frontend.skills.reyna;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.Skill;

public class Splash extends Skill {
    protected int basePower;
    protected int upgradeCount;

    public Splash() {
        super(
            "Splash",
            "punch",
            TargetScope.SINGLE,
            50,
            100,
            Elements.WATER
        );
    }

    @Override
    public void skill_logic(Battler user, Battler target) {
        // TODO: Create Skill Logic
        target.takeDamage(damage);
        System.out.println(user.getName() + " uses Splash!");
        System.out.println(user.getName() + " dealt " + damage + " damage to " + target.getName() + "!");
    }

    @Override
    public void upgrade(int count) {
        // TODO: Create Upgrade Logic
        switch (upgradeCount) {
            case 1:
                this.basePower = 60;
            case 2:
                this.basePower = 70;
            default:
                this.basePower = 50;
        }
    }

}
