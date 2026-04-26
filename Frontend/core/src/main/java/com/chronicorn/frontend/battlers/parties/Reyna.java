package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.reyna.Splash;
import com.chronicorn.frontend.skills.sailor.SecondWind;
import com.chronicorn.frontend.skills.sailor.SpreadTheBlade;

public class Reyna extends Actor {
    public Reyna() {
        super(
            "Reyna",
            35,
            20,
            20,
            20,
            20,
            20
        );

        Skill firstSkill = new Splash();
        Skill secondSkill = new SpreadTheBlade();
        Skill thirdSkill = new SecondWind();
        Skill ultimate;

        this.element = Elements.WATER;

        this.skills.add(
            firstSkill,
            secondSkill,
            thirdSkill
        );
    }
}
