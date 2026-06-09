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
                for (int i = 0; i < defender.getActiveStates().size; i++) {
                    StatusEffect se = defender.getActiveStates().get(i);
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

                owner.requestFollowUp("oathbearer_strike", owner);

                // 2. Process Team Heal tick immediately: Restores 25% of Reyna's Max HP to all
                // allies
                int outOfTurnHeal = (int) Math.floor(owner.getMaxHp() * 0.25f);
                boolean healedAlly = false;
                if (GameSession.getInstance().getParty() != null
                        && GameSession.getInstance().getParty().getActivePartyActors() != null) {
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
            private Battler currentMarkedTarget = null;

            @Override
            public void onApply(Battler owner, StatusEffect statusEffect) {
                if (currentMarkedTarget != null && currentMarkedTarget != owner) {
                    currentMarkedTarget.removeStatusEffect("dominique_marker");
                }
                currentMarkedTarget = owner;
                statusEffect.setSavedValue("stacks", 0.0f);
                if (owner instanceof Enemy) {
                    statusEffect.setSavedValue("preWeaknessBarSnapshot",
                            (float) ((Enemy) owner).getWeaknessBar());
                } else {
                    statusEffect.setSavedValue("preWeaknessBarSnapshot", 0.0f);
                }
            }

            @Override
            public float onModifyHiddenParam(Battler owner, Battler origin, int statId, float currentValue) {
                if (statId == 0 && owner == currentMarkedTarget) {
                    for (int i = 0; i < owner.getActiveStates().size; i++) {
                        StatusEffect state = owner.getActiveStates().get(i);
                        if (state.getId().equals("dominique_marker")) {
                            return currentValue + (state.getSavedValue("stacks") * 0.01f);
                        }
                    }
                }
                return currentValue;
            }

            @Override
            public int onReact(Battler attacker, Battler defender, Skill skill, int incomingDamage) {
                if (defender == currentMarkedTarget && defender instanceof com.chronicorn.frontend.battlers.Enemy) {
                    for (StatusEffect state : defender.getActiveStates()) {
                        if (state.getId().equals("dominique_marker")) {
                            state.setSavedValue("preWeaknessBarSnapshot",
                                    ((com.chronicorn.frontend.battlers.Enemy) defender).getWeaknessBar());
                            break;
                        }
                    }
                }
                return incomingDamage;
            }

            @Override
            public void onRespond(Battler attacker, Battler defender, Skill skill, int damageTaken) {
                if (defender != currentMarkedTarget)
                    return;

                if (defender instanceof com.chronicorn.frontend.battlers.Enemy) {
                    StatusEffect state = null;
                    for (StatusEffect se : defender.getActiveStates()) {
                        if (se.getId().equals("dominique_marker")) {
                            state = se;
                            break;
                        }
                    }

                    if (state != null) {
                        float currentWeakBar = ((com.chronicorn.frontend.battlers.Enemy) defender).getWeaknessBar();
                        float preWeaknessBarSnapshot = state.getSavedValue("preWeaknessBarSnapshot");

                        if (currentWeakBar < preWeaknessBarSnapshot) {
                            float breakdownDelta = preWeaknessBarSnapshot - currentWeakBar;
                            float currentStacks = state.getSavedValue("stacks");
                            float finalStacks = Math.min(currentStacks + breakdownDelta, 100.0f);
                            state.setSavedValue("stacks", finalStacks);
                            System.out.println("Marker Stacks updated: " + finalStacks);
                        }

                        if (skill.getSkillType().startsWith("reaction_")) {
                            state.setSavedValue("stacks", 0.0f);
                            System.out.println("Reaction consumed the mark. Stacks dropped to 0.");
                        }
                    }
                }
            }

            @Override
            public void onLeave(Battler owner, StatusEffect statusEffect) {
                if (owner == currentMarkedTarget) {
                    currentMarkedTarget = null;
                    System.out.println("True Marker holder left the field. Global memory cleared cleanly.");
                }
            }
        });

        logicMap.put("smothered", new StatusLogic() {
            @Override
            public int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
                if (statId == 0) { // Attack
                    return Math.round(currentValue * 0.5f);
                }
                return currentValue;
            }

            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                System.out.println(owner.getName() + " is smothered!");
            }
        });

        logicMap.put("damp", new StatusLogic() {
            @Override
            public int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
                if (statId == 1) { // Defense
                    return Math.round(currentValue * 0.67f);
                }
                return currentValue;
            }

            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                System.out.println(owner.getName() + " is damp!");
            }
        });

        logicMap.put("muddied", new StatusLogic() {
            @Override
            public int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
                if (statId == 3) { // Speed
                    return Math.round(currentValue * 0.67f);
                }
                return currentValue;
            }

            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                System.out.println(owner.getName() + " is muddied!");
            }
        });

        logicMap.put("isolate", new StatusLogic() {
            @Override
            public void onTurnEnd(Battler owner, Battler origin) {
                System.out.println(owner.getName() + " is isolated!");
            }
        });

    }

    public static StatusLogic getLogic(String status) {
        return logicMap.get(status);
    }
}
