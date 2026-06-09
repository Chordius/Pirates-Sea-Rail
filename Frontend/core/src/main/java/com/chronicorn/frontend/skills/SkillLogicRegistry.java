package com.chronicorn.frontend.skills;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.statuseffect.StatusEffect;
import com.chronicorn.frontend.managers.eventManagers.GameSession;

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
                    target.addStatusEffect("wind_scar");
                }
            }
        });

        logicMap.put("second_wind", (user, target, baseDamage, elements) -> {
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

        // ===================================
        // Porter's Skill Logic
        // ===================================
        logicMap.put("fire_punch", (user, target, baseDamage, elements) -> {
            target.takeDamage(baseDamage, elements);
        });

        // First Skill: Blazing Eagle
        logicMap.put("blazing_eagle", new SkillLogic() {
            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                // Main Target takes the standard calculated damage
                target.takeDamage(baseDamage, element);

                // Apply Speed Debuff to main target as specified in the GDD
                target.addStatusEffect("hot_talons", user);
            }
        });

        // Second Skill: Falcon Dive
        logicMap.put("falcon_dive", new SkillLogic() {
            private float primaryWeaknessBefore;

            @Override
            public void before(Battler user, Battler target, Skill skill) {
                if (target instanceof Enemy) {
                    primaryWeaknessBefore = ((Enemy) target).getWeaknessBar();
                }
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);

                if (target instanceof Enemy) {
                    float primaryWeaknessAfter = ((Enemy) target).getWeaknessBar();

                    // If the action caused a Weakness Break (reduced bar down to 0)
                    if (primaryWeaknessBefore > 0 && primaryWeaknessAfter <= 0) {
                        target.addStatusEffect("hawk_eye", user);
                    }
                }
            }
        });

        // Ultimate: Flameforce Talon (Logic check runs in before() via your source code
        // layout)
        logicMap.put("flameforce_talon", new SkillLogic() {
            @Override
            public void before(Battler user, Battler target, Skill skill) {
                int targetSpeedDefault = target.getSpeed();
                int targetSpeedModified = target.getEffectivePrimaryParam(3); // SPE Parameter Array Index

                if (targetSpeedModified < targetSpeedDefault) {
                    skill.setAlwaysCrit(true);
                }
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
                // Applies unique mark or tracking state if necessary
                target.addStatusEffect("burn", user);
            }
        });

        // ===================================
        // Reyna's Skill Logic
        // ===================================
        logicMap.put("first_aid", (user, target, baseDamage, elements) -> {
            int healingBase = (int) Math.floor(user.getMaxHp() * 0.4f);
            int healingModifier = (int) (1 + target.getEffectiveSpParam(3));
            int healingFlat = 10;

            int healingAmount = healingBase * healingModifier + healingFlat;
            target.heal(healingAmount, elements);

            target.addStatusEffect("pristine_health", user);
        });

        logicMap.put("heed_this_call", (user, target, baseDamage, elements) -> {
            // Deal damage to target first
            target.takeDamage(baseDamage, elements);

            // Calculate 20% lifesteal splash back as general group utility healing
            int groupHeal = (int) Math.floor(baseDamage * 0.50f);

            // Loop back through active allies to process healing redistribution
            boolean healedAlly = false;
            if (GameSession.getInstance().getParty() != null && GameSession.getInstance().getParty().getActivePartyActors() != null) {
                for (Actor ally : GameSession.getInstance().getParty().getActivePartyActors()) {
                    if (ally != null && ally.isAlive()) {
                        ally.heal(groupHeal, elements);
                        healedAlly = true;
                    }
                }
            }
            if (!healedAlly && user.isAlive()) {
                user.heal(groupHeal, elements);
            }
        });

        logicMap.put("hippocratic_oath", (user, target, baseDamage, elements) -> {
            // Applies the active combat loop modifier to Reyna herself
            user.addStatusEffect("oathbearer", user);
        });

        logicMap.put("oathbearer_strike", (user, target, baseDamage, elements) -> {
            target.takeDamage(baseDamage, elements);
        });

        // ===================================
        // Deal's Skill Logic
        // ===================================
        final java.util.function.BiConsumer<Battler, Battler> applyOrRefreshMarker = (user, target) -> {
            // Enforce single-ownership layout across all active entities in the battle
            // manager
            for (StatusEffect state : target.getActiveStates()) {
                if (state.getId().equals("dominique_marker")) {
                    state.setTurns(99); // Reset duration if already active
                    return;
                }
            }

            // Scrub active marker states off any other targets currently on the field
            // Note: In your full game loop, cleanly call allBattlers from the battle
            // manager context
            target.addStatusEffect("dominique_marker", user);
        };

        // Basic Shoot Command
        logicMap.put("deal_shoot", new SkillLogic() {
            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
            }

            @Override
            public boolean isShowConditionMet(Battler user) {
                for (StatusEffect state : user.getActiveStates()) {
                    if ("ace_mode".equals(state.getId())) {
                        return false;
                    }
                }
                return true;
            }
        });

        // Enhanced Basic Command: Remarkable Shot!
        logicMap.put("remarkable_shot", new SkillLogic() {
            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
            }

            @Override
            public boolean isShowConditionMet(Battler user) {
                for (StatusEffect state : user.getActiveStates()) {
                    if ("ace_mode".equals(state.getId())) {
                        return true;
                    }
                }
                return false;
            }
        });

        // First Skill: Ascending Mark
        logicMap.put("ascending_mark", new SkillLogic() {
            @Override
            public void before(Battler user, Battler target, Skill skill) {
                applyOrRefreshMarker.accept(user, target);
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                target.takeDamage(baseDamage, element);
            }
        });

        // Second Skill: Ace Archer
        logicMap.put("ace_archer", (user, target, baseDamage, elements) -> {
            user.addStatusEffect("ace_mode", user);
        });

        // Ultimate: One-Shot
        logicMap.put("one_shot", new SkillLogic() {
            @Override
            public void before(Battler user, Battler target, Skill skill) {
                applyOrRefreshMarker.accept(user, target);
            }

            @Override
            public void execute(Battler user, Battler target, int baseDamage, Elements element) {
                // Process damage registration
                target.takeDamage(baseDamage, element);

                // Locate the status instance to inject overflow bonus damage stacks
                for (StatusEffect state : target.getActiveStates()) {
                    if (state.getId().equals("dominique_marker")) {
                        float bonusStacks = (float) Math.floor(baseDamage * 0.30f);
                        float currentStacks = state.getSavedValue("stacks");

                        // Ultimate bonus stacks can explicitly break past the default 100 soft cap up
                        // to an absolute limit of 200
                        float finalStacks = Math.min(currentStacks + bonusStacks, 200.0f);
                        state.setSavedValue("stacks", finalStacks);

                        System.out.println(
                                "Marker Overflow! Bonus Stacks applied: " + bonusStacks + " | Total: " + finalStacks);
                        break;
                    }
                }
            }
        });

    }

    public static SkillLogic getLogic(String skillId) {
        return logicMap.getOrDefault(skillId, logicMap.get("default"));
    }
}
