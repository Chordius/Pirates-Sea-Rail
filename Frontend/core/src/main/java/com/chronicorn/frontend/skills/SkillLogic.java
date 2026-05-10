package com.chronicorn.frontend.skills;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public interface SkillLogic {
    void execute(Battler user, Battler target, int BaseDamage, Elements element);
    default void before(Battler user, Battler target, Skill skill) {};
}
