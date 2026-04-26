package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.sailor.SecondWind;
import com.chronicorn.frontend.skills.sailor.SonicSlash;
import com.chronicorn.frontend.skills.sailor.SpreadTheBlade;

public class Sailor extends Actor {
    public Sailor() {
        super(
            "Sailor",
            35,
            110,
            20,
            20,
            20,
            20
        );

        Skill firstSkill = new SonicSlash();
        Skill secondSkill = new SpreadTheBlade();
        Skill thirdSkill = new SecondWind();
        Skill ultimate;

        this.element = Elements.WIND;

        this.skills.add(
            firstSkill,
            secondSkill,
            thirdSkill
        );
    }
}
