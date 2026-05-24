package com.chronicorn.frontend.statuseffect;

import com.badlogic.gdx.graphics.Color;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.managers.eventManagers.GameSession;

import java.util.HashMap;

public class StatusEffectRegistry {
    private static HashMap<String, StatusLogic> logicMap = new HashMap<>();

    // Initialize all your unique mechanics here
    public static void init() {
        logicMap.put("burn", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                int basePower = 15;
                int burnDamage = (int) ((((double) (2 * origin.getLevel()) / 5 + 2) * basePower
                        * ((double) origin.getEffectivePrimaryParam(2) / owner.getEffectivePrimaryParam(1)) * 0.02
                        + 2));
                owner.takeDamage(burnDamage, Elements.FIRE);
                System.out.println(owner.getName() + "takes Burn DMG!");
            }
        });

        logicMap.put("electrocuted", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                int basePower = 10;
                int electrocuteDMG = (int) ((((double) (2 * origin.getLevel()) / 5 + 2) * basePower
                        * ((double) origin.getEffectivePrimaryParam(2) / owner.getEffectivePrimaryParam(1)) * 0.02
                        + 2));
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
            public int onConfirm(Battler attacker, Battler defender, StatusEffect state, Skill skill,
                    int incomingDamage) {
                if (defender instanceof Enemy) {
                    int weakBarValue = ((Enemy) defender).getWeaknessBar();
                    state.setSavedValue("previousWeakBarValue", weakBarValue);
                    System.out.println("Weakness Value Before Hit: " + weakBarValue);
                }
                return incomingDamage;
            }

            @Override
            public void onEstablish(Battler attacker, Battler defender, StatusEffect state, Skill skill,
                    int incomingDamage) {
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

            private void triggerEmergencyHeal(String eventSource, Battler owner, StatusEffect state) {
                // 1. Check the local state map to see if this specific buff instance already
                // popped
                if (state.getSavedValue("emergencyTriggered") == 1.0f) {
                    return;
                }

                // 2. Mark this unique state instance as triggered
                state.setSavedValue("emergencyTriggered", 1.0f);

                // 3. Execute healing logic
                Battler origin = state.getOrigin();
                int emergencyHeal = (int) Math.floor(origin.getMaxHp() * 0.40f);
                owner.heal(emergencyHeal, Elements.WATER);
                owner.requestPopup("Pristine Burst!", Color.GREEN);

                System.out.println("Triggered via " + eventSource + " for " + owner.getName());
            }

            @Override
            public void onApply(Battler owner, StatusEffect statusEffect) {
                // Initialize the flag on this unique state instance when first applied
                statusEffect.setSavedValue("emergencyTriggered", 0.0f);
            }

            @Override
            public float onModifySpParam(Battler owner, Battler origin, int statId, float currentValue) {
                if (statId == 3) { // Incoming Healing
                    return currentValue + 0.30f;
                }
                return currentValue;
            }

            @Override
            public int onReact(Battler attacker, Battler defender, Skill skill, int incomingDamage) {
                // Find this specific character's unique active state instance
                StatusEffect localState = null;
                for (StatusEffect se : defender.getActiveStates()) {
                    if (se.getId().equals("pristine_health")) {
                        localState = se;
                        break;
                    }
                }

                if (localState != null) {
                    int predictedHp = defender.getHp() - incomingDamage;
                    int threshold = (int) Math.floor(defender.getMaxHp() * 0.33f);

                    if (predictedHp <= threshold || predictedHp <= 0) {
                        triggerEmergencyHeal("onReact Threshold", defender, localState);
                    }
                }
                return incomingDamage;
            }

            @Override
            public void onLeave(Battler owner, StatusEffect statusEffect) {
                // Check natural decay on expiration loop safely using its own instance data
                if (statusEffect.getTurns() <= 0) {
                    triggerEmergencyHeal("onLeave Decay", owner, statusEffect);
                }
            }
        });

        logicMap.put("oathbearer", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                System.out.println(owner.getName() + " channels the Hippocratic Oath!");

                // 1. Queue an official out-of-turn Follow-up Attack action across the engine
                // queue
                // Passing 'owner' as the target; your Action resolver will expand this to
                // ALL_ENEMIES based on the JSON scope
                owner.requestFollowUp("oathbearer_strike", owner);

                // 2. Process Team Heal tick immediately: Restores 25% of Reyna's Max HP to all
                // allies
                int outOfTurnHeal = (int) Math.floor(owner.getMaxHp() * 0.25f);
                boolean healedAlly = false;
                if (GameSession.getInstance().getParty() != null && GameSession.getInstance().getParty().getActivePartyActors() != null) {
                    for (Actor ally : GameSession.getInstance().getParty().getActivePartyActors()) {
                        if (ally != null && ally.isAlive()) {
                            ally.heal(outOfTurnHeal, Elements.WATER);
                            healedAlly = true;
                        }
                    }
                }
                if (!healedAlly && owner.isAlive()) {
                    owner.heal(outOfTurnHeal, Elements.WATER);
                }
            }
        });

        logicMap.put("ace_mode", new StatusLogic() {
            @Override
            public void onApply(Battler owner, StatusEffect statusEffect) {
                // TODO: Configure UI
                // Dynamically clear standard basic attack asset configurations and learn the
                // enhanced version
                // For production systems, you can hook this to your UI selector panel or
                // inputtingAction routing pipelines
                System.out.println(owner.getName() + " assumes Ace Stance!");
            }

            @Override
            public void onLeave(Battler owner, StatusEffect statusEffect) {
                System.out.println(owner.getName() + " exits Ace Stance.");
            }
        });

        logicMap.put("dominique_marker", new StatusLogic() {
            // Global variables stored on the single shared instance memory block
            private Battler currentMarkedTarget = null;
            private float globalMarkerStacks = 0.0f;
            private float preWeaknessBarSnapshot = 0.0f;

            @Override
            public void onApply(Battler owner, StatusEffect statusEffect) {
                // If switching targets, safely reset the global stack counter for the new owner
                if (currentMarkedTarget != owner) {
                    currentMarkedTarget = owner;
                    globalMarkerStacks = 0.0f;
                    System.out.println("Marker ownership shifted to: " + owner.getName() + ". Stacks reset globally.");
                }
                preWeaknessBarSnapshot = 0.0f;
            }

            @Override
            public float onModifyHiddenParam(Battler owner, Battler origin, int statId, float currentValue) {
                // Index 0: ReactionDMGBonus
                // Only amplify damage if the entity requesting the stat is the TRUE active
                // marker holder
                if (statId == 0 && owner == currentMarkedTarget) {
                    return currentValue + (globalMarkerStacks * 0.01f);
                }
                return currentValue;
            }

            @Override
            public int onConfirm(Battler attacker, Battler defender, StatusEffect state, Skill skill,
                    int incomingDamage) {
                // Snapshot defense data only if processing the true target
                if (defender == currentMarkedTarget && defender instanceof com.chronicorn.frontend.battlers.Enemy) {
                    preWeaknessBarSnapshot = ((com.chronicorn.frontend.battlers.Enemy) defender).getWeaknessBar();
                }
                return incomingDamage;
            }

            @Override
            public void onEstablish(Battler attacker, Battler defender, StatusEffect state, Skill skill,
                    int damageDealt) {
                if (defender != currentMarkedTarget)
                    return;

                if (defender instanceof com.chronicorn.frontend.battlers.Enemy) {
                    float currentWeakBar = ((com.chronicorn.frontend.battlers.Enemy) defender).getWeaknessBar();

                    if (currentWeakBar < preWeaknessBarSnapshot) {
                        float breakdownDelta = preWeaknessBarSnapshot - currentWeakBar;
                        globalMarkerStacks = Math.min(globalMarkerStacks + breakdownDelta, 100.0f);
                        System.out.println("Global Marker Stacks updated: " + globalMarkerStacks);
                    }
                }

                // Wipe the global variables cleanly if a reaction consumed it
                if (skill.getSkillType().startsWith("reaction_")) {
                    globalMarkerStacks = 0.0f;
                    System.out.println("Reaction consumed the mark. Global stacks dropped to 0.");
                }
            }

            @Override
            public void onLeave(Battler owner, StatusEffect statusEffect) {
                // Only clean up the global memory if the target leaving is the actual active
                // holder
                if (owner == currentMarkedTarget) {
                    currentMarkedTarget = null;
                    globalMarkerStacks = 0.0f;
                    System.out.println("True Marker holder left the field. Global memory cleared cleanly.");
                }
            }
        });

        logicMap.put("smothered", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                // Define what Smothered does (e.g., reduce ATK each turn, or just a visual
                // marker)
                System.out.println(owner.getName() + " is smothered!");
            }
        });

    }

    public static StatusLogic getLogic(String status) {
        return logicMap.get(status);
    }
}
