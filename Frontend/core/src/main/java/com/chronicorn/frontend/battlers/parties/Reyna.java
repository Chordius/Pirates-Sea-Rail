package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class Reyna extends Actor {
    public Reyna() {
        super(
            "Reyna",
            75,
            110,
            40,
            80,
            75,
            20
        );

        this.learnSkill("splash");
        this.learnSkill("first_aid");

        this.element = Elements.WATER;
    }
}
