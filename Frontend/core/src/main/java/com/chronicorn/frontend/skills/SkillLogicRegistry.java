package com.chronicorn.frontend.skills;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.statuseffect.StatusEffect;

import java.util.HashMap;

public class SkillLogicRegistry {
    private static HashMap<String, SkillLogic> logicMap = new HashMap<>();

    // Initialize all your unique mechanics here
    public static void init() {
        // Example 1: Standard Attack (Fallback)
        logicMap.put("default", (user, target, baseDamage, elements) -> {
            target.takeDamage(baseDamage, elements);
        });

        logicMap.put("default_heal", (user, target, baseDamage, elements) -> {
            float baseRate = 0.2f;
            int healingBase = (int) Math.floor(user.getMaxHp() * baseRate);
            int healingModifier = (int) (1 + target.getEffectiveSpParam(4));
            int healingFlat = 0;

            int healingAmount = healingBase * healingModifier + healingFlat;
            target.heal(healingAmount, elements);
        });

        // Sailor's Skill Logic
        logicMap.put("super_sonic_slash", new SkillLogic() {
            @Override
            public void before(Battler user, Battler target, Skill skill) {
                Elements element = (Elements) ((Actor) user).getTempVar("secondWindFollowElement");
                if (element != null) {
                    skill.setElement(element);
                }
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
            }
        });

        logicMap.put("wind_cutter", new SkillLogic() {
            float prevWeakBar;

            @Override
            public void before(Battler user, Battler target, Skill skill) {
                if (target instanceof Enemy) {
                    prevWeakBar = ((Enemy) target).getWeaknessBar();
                }
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                float currWeakBar = ((Enemy) target).getWeaknessBar();

                target.takeDamage(baseDamage, element);

                if (prevWeakBar != currWeakBar) {
                    // TODO: Inflict RES Down
                    target.addStatusEffect("wind_scar");
                }
            }
        });

        logicMap.put("second_wind", (user, target, baseDamage, elements) -> {
            // TODO: Add MAG Up via State "Second Wind"
            if (target instanceof Actor) {
                target.addStatusEffect("second_wind", user);
            }
        });

        logicMap.put("the_wind_way", (user, target, baseDamage, elements) -> {
            target.takeDamage(baseDamage, elements);
            if (target instanceof Enemy) {
                int breakamount = 90;
                if (((Enemy) target).getWeaknessBar() <= breakamount) {
                    ((Enemy) target).reduceWeaknessBar(1);
                } else {
                    ((Enemy) target).reduceWeaknessBar(breakamount);
                }
            }
        });

        // Porter's Skill Logic
        logicMap.put("flameforce_talon", new SkillLogic() {
            @Override
            public void before(Battler user, Battler target, Skill skill) {
                int targetSpeedDefault = target.getSpeed();
                int targetSpeedModified = target.getEffectivePrimaryParam(3);

                if (targetSpeedDefault > targetSpeedModified) {
                    skill.setAlwaysCrit(true);
                }
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
            }
        });

        // Reyna's Skill Logic
        logicMap.put("first_aid", (user, target, baseDamage, elements) -> {
            int healingBase = (int) Math.floor(user.getMaxHp() * 0.2f);
            int healingModifier = (int) (1 + target.getEffectiveSpParam(3));
            int healingFlat = 200;

            int healingAmount = healingBase * healingModifier + healingFlat;
            target.heal(healingAmount, elements);

            // TODO: Add "Pristine Health" state
            target.addStatusEffect("pristine_health", user);
        });

        logicMap.put("heed_this_call", (user, target, baseDamage, elements) -> {
            int lifesteal = (int) Math.floor(0.2 * baseDamage);
            target.takeDamage(baseDamage, elements);
            target.heal(lifesteal, elements);
        });
    }

    public static SkillLogic getLogic(String skillId) {
        return logicMap.getOrDefault(skillId, logicMap.get("default"));
    }
}
