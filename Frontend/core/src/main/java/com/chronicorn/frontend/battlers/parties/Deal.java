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
            21
        );

        this.setId("C004");
        this.learnSkill("deal_shoot");
        this.learnSkill("remarkable_shot");
        this.learnSkill("ascending_mark");
        this.learnSkill("ace_archer");
        this.learnSkill("one_shot");

        this.element = Elements.LIGHTNING;
    }
}
