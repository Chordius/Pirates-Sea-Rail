package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class Deal extends Actor {
    public Deal() {
        super(
            "Deal",
            59,
            120,
            65,
            60,
            90,
            20
        );

        this.learnSkill("deal_shoot");

        this.element = Elements.LIGHTNING;
    }
}
