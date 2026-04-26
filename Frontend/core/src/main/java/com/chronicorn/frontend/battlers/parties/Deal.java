package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.deal.Shoot;
import com.chronicorn.frontend.skills.sailor.SecondWind;
import com.chronicorn.frontend.skills.sailor.SpreadTheBlade;

public class Deal extends Actor {
    public Deal() {
        super(
            "Deal",
            35,
            20,
            20,
            20,
            20,
            20
        );

        Skill firstSkill = new Shoot();
        Skill secondSkill = new SpreadTheBlade();
        Skill thirdSkill = new SecondWind();
        Skill ultimate;

        this.element = Elements.LIGHTNING;

        this.skills.add(
            firstSkill,
            secondSkill,
            thirdSkill
        );
    }
}
