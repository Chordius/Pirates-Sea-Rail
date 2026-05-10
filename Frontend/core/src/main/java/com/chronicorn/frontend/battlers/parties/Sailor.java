package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class Sailor extends Actor {
    public Sailor() {
        super(
            "Sailor",
            60,
            110,
            75,
            60,
            83,
            20
        );

        this.learnSkill("sonic_slash");
        this.learnSkill("wind_cutter");
        this.learnSkill("second_wind");
        this.learnSkill("the_wind_way");

        this.element = Elements.WIND;
    }
}
