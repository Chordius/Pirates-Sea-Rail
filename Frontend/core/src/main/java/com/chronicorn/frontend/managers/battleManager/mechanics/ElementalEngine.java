package com.chronicorn.frontend.managers.battleManager.mechanics;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.ReactionType;
import com.chronicorn.frontend.skills.Skill;

import java.util.HashMap;

public class ElementalEngine {
    private static final Array<Elements> sampledElements = new Array<>();

    // Example lookup method
    public static ReactionType getReaction(HashMap<Elements, Elements> elementsHashMap) {
        if (elementsHashMap.get(Elements.FIRE) == Elements.WATER) return ReactionType.DOUSE;
        if (elementsHashMap.get(Elements.WATER) == Elements.FIRE) return ReactionType.EVAPORATE;

        return ReactionType.NONE;
    }

    public static int processImpact(Skill selectedSkill, int incomingDamage, Actor source, Enemy target, boolean primary) {
        Elements attackElement = selectedSkill.getElement();
        int ToughnessDMG = selectedSkill.getToughnessDMG();

        if (primary) {
            sampledElements.clear();
        }

        // --- WIND CAMOUFLAGE ---
        if (attackElement == Elements.WIND) {
            // Wind changes into the target's element or existing mark.
            attackElement = determineWindMutation(target, primary);
        }

        // --- SIGNATURE LOSS & REACTIONS ---
        if (target.isInSignatureLoss()) {

            ElementMark existingMark = target.getCurrentElementalMark();
            if (existingMark != null) {
                System.out.println("Enemy's Mark: " + existingMark.element.name());
            }

            if (existingMark != null && existingMark.element != attackElement) {
                // A Reaction Occurs! (e.g., Fire meets Water)
                incomingDamage = triggerReaction(existingMark.element, attackElement, incomingDamage);
                target.clearElementalMark(); // Marks are consumed
            } else {
                // Apply a new mark if they don't have one, or overwrite if same
                System.out.println("Apply a new mark!");
                target.applyElementalMark(attackElement, source);
                System.out.println("Enemy's new Mark: " + target.getCurrentElementalMark().element.name());
            }

        }
        // --- PHASE 3: THE BREAK CHECK ---
        else {
            // Target is NOT in Signature Loss. Can we break them?
            if (isWeakness(attackElement, target.getInnateElements())) {

                target.reduceWeaknessBar(ToughnessDMG);

                if (target.getWeaknessBar() <= 0) {
                    System.out.println("BREAK!");
                    incomingDamage = triggerBreak(attackElement, target, incomingDamage);
                }
            }
        }

        return incomingDamage;
    }

    private static boolean isWeakness(Elements attackElement, Array<Elements> targetElements) {
        for (Elements element : targetElements) {
            if (attackElement.getResistance() == element) {
                return true;
            }
        }
        return false;
    }

    private static int triggerBreak(Elements breakingElement, Enemy target, int damage) {
        target.setSignatureLoss(true); // Enter Signature Loss for 1 turn

        // GDD: Breaking skill always inflicts specific states
        switch (breakingElement) {
            case FIRE: target.addStatusEffect("Burn"); break;
            case WATER: target.addStatusEffect("Douse"); break;
            case LIGHTNING: target.addStatusEffect("Electrocute"); break;
            case EARTH: target.addStatusEffect("Isolate"); break;
        }

        return triggerReaction(breakingElement, breakingElement.getResistance(), damage);
    }

    private static int triggerReaction(Elements mark, Elements trigger, int damage) {
        // GDD Reaction Logic
        HashMap<Elements, Elements> elements = new HashMap<>();
        elements.put(mark, trigger);

        switch (getReaction(elements)) {
            case DOUSE:
                System.out.println("Douse! Damage x1.5");
                return (int) (damage * 1.5);
            case EVAPORATE:
                System.out.println("Evaporate! Damage x1.5");
                return (int) (damage * 1.5);
            default:
                System.out.println("Reaction Triggered!");
                break;
        }

        return damage;
    }

    private static Elements determineWindMutation(Enemy target, boolean primary) {
        // Enemies could have different kinds of elements.
        // We sample for the best candidate to convert wind into that element.
        Array<Elements> windPotential = target.getInnateElements();

        // If we don't have a sample of the first enemy's elements, sample it.
        if (primary) {
            sampledElements.addAll(windPotential);
        }

        // We scroll through the first enemy's elements, and find the element
        // that could break the current enemy's elements.
        for (Elements t : sampledElements) {
            if (isWeakness(t, windPotential)) {
                return t;
            }
        }
        return sampledElements.get(0);
    }

    private static void clean() {
        // Set firstElementWind back to 0.
        sampledElements.clear();
    }
}
