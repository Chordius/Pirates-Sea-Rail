package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.porter.FirePunch;
import com.chronicorn.frontend.skills.sailor.SecondWind;
import com.chronicorn.frontend.skills.sailor.SpreadTheBlade;

public class Porter extends Actor {
    public Porter() {
        super(
            "Porter",
            35,
            120,
            20,
            20,
            20,
            21
        );

        Skill firstSkill = new FirePunch();
        Skill secondSkill = new SpreadTheBlade();
        Skill thirdSkill = new SecondWind();
        Skill ultimate;

        this.element = Elements.FIRE;

        this.skills.add(
            firstSkill,
            secondSkill,
            thirdSkill
        );
    }
}
