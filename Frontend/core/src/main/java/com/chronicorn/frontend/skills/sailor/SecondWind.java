package com.chronicorn.frontend.skills.sailor;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.Skill;

public class SecondWind extends Skill {
    protected int basePower;
    protected int upgradeCount;

    public SecondWind() {
        super(
            "Second Wind",
            "upgrade",
            TargetScope.ALL,
            0,
            200,
            Elements.WIND
        );
    }

    @Override
    public void skill_logic(Battler user, Battler target) {
        // TODO: Create Skill Logic
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
