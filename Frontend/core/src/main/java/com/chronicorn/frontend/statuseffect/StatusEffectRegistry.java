package com.chronicorn.frontend.statuseffect;

import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;

import java.util.HashMap;

public class StatusEffectRegistry {
    private static HashMap<String, StatusLogic> logicMap = new HashMap<>();

    // Initialize all your unique mechanics here
    public static void init() {
        logicMap.put("burn", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                int basePower = 15;
                int burnDamage = (int) ((((double) (2 * origin.getLevel()) / 5 + 2) * basePower * ((double) origin.getEffectivePrimaryParam(2) / owner.getEffectivePrimaryParam(1)) * 0.02 + 2));
                owner.takeDamage(burnDamage, Elements.FIRE);
                System.out.println(owner.getName() + "takes Burn DMG!");
            }
        });

        logicMap.put("electrocuted", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                int basePower = 10;
                int electrocuteDMG = (int) ((((double) (2 * origin.getLevel()) / 5 + 2) * basePower * ((double) origin.getEffectivePrimaryParam(2) / owner.getEffectivePrimaryParam(1)) * 0.02 + 2));
                owner.takeDamage(electrocuteDMG, Elements.LIGHTNING);
                System.out.println(owner.getName() + "takes Electrocute DMG!");
            }
        });

        logicMap.put("wind_scar", new StatusLogic() {
            @Override
            public float onModifySpParam(Battler owner, Battler origin, int statId, float currentValue) {
                // If the game is asking for SpParam 5, reduce it by 14%
                if (statId == 5) {
                    return currentValue * 0.86f;
                }

                // If it's asking for any other stat, leave it alone
                return currentValue;
            }
        });

        logicMap.put("second_wind", new StatusLogic() {
            Battler origin = null;

            @Override
            public int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
                this.origin = origin;
                float bonus = origin.getEffectivePrimaryParam(2) * 0.25f;

                if (statId == 2) {
                    return Math.round(currentValue * (1 + bonus));
                }

                // If it's asking for any other stat, leave it alone
                return currentValue;
            }

            @Override
            public int onConfirm(Battler attacker, Battler defender, StatusEffect state, Skill skill, int incomingDamage) {
                if (defender instanceof Enemy) {
                    int weakBarValue = ((Enemy) defender).getWeaknessBar();
                    state.setSavedValue("previousWeakBarValue", weakBarValue);
                    System.out.println("Weakness Value Before Hit: " + weakBarValue);
                }
                return incomingDamage;
            }

            @Override
            public void onEstablish(Battler attacker, Battler defender, StatusEffect state, Skill skill, int incomingDamage) {
                if (defender instanceof Enemy) {
                    int weakBarValueNow = ((Enemy) defender).getWeaknessBar();
                    System.out.println("Weakness Value Now: " + weakBarValueNow);
                    int weakBarValuePrev = (int) state.getSavedValue("previousWeakBarValue");
                    System.out.println("Weakness Value Prev: " + weakBarValuePrev);

                    if (weakBarValueNow < weakBarValuePrev) {
                        System.out.println("Launch a Follow-Up!");
                        origin.setTempVar("secondWindFollowElement", ((Actor) attacker).getElement());
                        origin.requestFollowUp("super_sonic_slash", defender);
                    }
                }
            }
        });

        logicMap.put("hawk_eye", new StatusLogic() {
            @Override
            public float onModifyHiddenParam(Battler owner, Battler origin, int statId, float currentValue) {
                float bonus = 0.2f;

                if (statId == 0) {
                    return Math.round(currentValue + bonus);
                }

                // If it's asking for any other stat, leave it alone
                return currentValue;
            }
        });

        logicMap.put("hot_talons", new StatusLogic() {
            @Override
            public int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
                float bonus = 10f;

                if (statId == 4) {
                    return Math.round(currentValue - bonus);
                }

                // If it's asking for any other stat, leave it alone
                return currentValue;
            }
        });

        logicMap.put("pristine_health", new StatusLogic() {
            @Override
            public float onModifySpParam(Battler owner, Battler origin, int statId, float currentValue) {
                float bonus = 0.3f;

                if (statId == 3) {
                    return currentValue + bonus;
                }

                return currentValue;
            }

            // TODO: On React
        });

        logicMap.put("smothered", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                // Define what Smothered does (e.g., reduce ATK each turn, or just a visual marker)
                System.out.println(owner.getName() + " is smothered!");
            }
        });


    }

    // TODO: Database job. Parse the get
    public static StatusLogic getLogic(String status) {
        return logicMap.get(status);
    }
}
