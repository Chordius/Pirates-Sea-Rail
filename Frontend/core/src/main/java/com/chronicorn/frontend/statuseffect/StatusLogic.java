package com.chronicorn.frontend.statuseffect;

import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.skills.Skill;

public interface StatusLogic {
    // --- Turn Timings ---
    default void onTurnStart(Battler owner) {}
    default void onTurnEnd(Battler owner, Battler origin) {}

    // --- Action Timings ---
    default void onActionStart(Battler owner) {}
    default void onActionEnd(Battler owner) {}

    // --- Damage Connects: Attacker (Subject) Timings ---
    // Occurs before damage math calculates
    default int onConfirm(Battler attacker, Battler defender, StatusEffect state, Skill skill, int incomingDamage) { return incomingDamage; }
    // Occurs after damage is dealt
    default void onEstablish(Battler attacker, Battler defender, StatusEffect state, Skill skill, int damageDealt) {}

    // --- Damage Connects: Defender (Target) Timings ---
    // Occurs before damage math calculates (e.g., Shields reducing damage)
    default int onReact(Battler attacker, Battler defender, Skill skill, int incomingDamage) { return incomingDamage; }
    // Occurs after damage is dealt (e.g., Thorns reflecting damage)
    default void onRespond(Battler attacker, Battler defender, Skill skill, int damageTaken) {}

    default void onLeave(Battler owner, StatusEffect statusEffect) {}
    default void onApply(Battler owner, StatusEffect statusEffect) {}

    default int onModifyPrimaryParam(Battler owner, Battler origin, int statId, int currentValue) {
        return currentValue;
    }

    default float onModifySpParam(Battler owner, Battler origin, int statId, float currentValue) {
        return currentValue;
    }

    default float onModifyHiddenParam(Battler owner, Battler origin, int statId, float currentValue) {
        return currentValue;
    }
}
