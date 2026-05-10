package com.chronicorn.frontend.battlers.parties;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public class Porter extends Actor {
    public Porter() {
        super(
            "Porter",
            50,
            120,
            85,
            20,
            50,
            21
        );

        this.learnSkill("fire_punch");

        this.element = Elements.FIRE;
    }
}
