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

        this.setId("C002");
        this.learnSkill("fire_punch");
        this.learnSkill("blazing_eagle");
        this.learnSkill("falcon_dive");
        this.learnSkill("flameforce_talon");

        this.element = Elements.FIRE;
    }
}
