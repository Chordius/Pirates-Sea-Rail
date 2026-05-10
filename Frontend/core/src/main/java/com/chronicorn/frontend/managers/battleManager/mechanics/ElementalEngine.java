package com.chronicorn.frontend.managers.battleManager.mechanics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.ReactionType;
import com.chronicorn.frontend.skills.Skill;

import java.util.HashMap;

public class ElementalEngine {
    private static final Array<Elements> sampledElements = new Array<>();

    // Reaction Lookup
    public static ReactionType getReaction(HashMap<Elements, Elements> elementsHashMap) {
        if (elementsHashMap.get(Elements.FIRE) == Elements.WATER) return ReactionType.DOUSE;
        if (elementsHashMap.get(Elements.FIRE) == Elements.EARTH) return ReactionType.SMOTHERED;
        if (elementsHashMap.get(Elements.FIRE) == Elements.LIGHTNING) return ReactionType.OVERLOAD;
        if (elementsHashMap.get(Elements.WATER) == Elements.FIRE) return ReactionType.EVAPORATE;
        if (elementsHashMap.get(Elements.WATER) == Elements.EARTH) return ReactionType.MUDDIED;
        if (elementsHashMap.get(Elements.WATER) == Elements.LIGHTNING) return ReactionType.ELECTROCUTE;
        if (elementsHashMap.get(Elements.EARTH) == Elements.FIRE) return ReactionType.BURN;
        if (elementsHashMap.get(Elements.EARTH) == Elements.WATER) return ReactionType.DAMP;
        if (elementsHashMap.get(Elements.LIGHTNING) == Elements.FIRE) return ReactionType.OVERLOAD;
        if (elementsHashMap.get(Elements.LIGHTNING) == Elements.WATER) return ReactionType.ELECTROCUTE;
        if (elementsHashMap.get(Elements.LIGHTNING) == Elements.EARTH) return ReactionType.ISOLATE;

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
            System.out.println("The Enemy is in Signature Loss!");

            ElementMark existingMark = target.getCurrentElementalMark();

            // If they already have a mark
            if (existingMark != null && existingMark.element != attackElement) {
                // A Reaction Occurs! (e.g., Fire meets Water)
                incomingDamage = triggerReaction(target, source, existingMark, attackElement, incomingDamage);
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
            System.out.println("The Enemy is NOT in Signature Loss!");
            // Target is NOT in Signature Loss. Can we break them?
            if (isWeakness(attackElement, target.getInnateElements())) {

                target.reduceWeaknessBar(ToughnessDMG);

                if (target.getWeaknessBar() <= 0) {
                    System.out.println("BREAK!");
                    System.out.println("Original DMG: " + incomingDamage);
                    incomingDamage = triggerBreak(attackElement, source, target, incomingDamage);
                    System.out.println("Post-Break DMG: " + incomingDamage);
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

    private static int triggerBreak(Elements breakingElement, Actor source, Enemy target, int damage) {
        target.setSignatureLoss(true);

        // We act as if a mark exists, even though it doesn't
        ElementMark tempMark = new ElementMark(breakingElement, source, 1);

        return triggerReaction(target, source, tempMark, breakingElement.getResistance(), damage);
    }

    private static int triggerReaction(Enemy target, Actor source, ElementMark mark, Elements trigger, int damage) {
        // GDD Reaction Logic
        int newDamage = damage;
        Elements markElement = mark.element;
        Actor source2 = (Actor) mark.source;
        Actor primarysource = source;
        Actor secondarySource = source2;

        HashMap<Elements, Elements> elements = new HashMap<>();
        elements.put(markElement, trigger);

        switch (getReaction(elements)) {
            case DOUSE:
                System.out.println("Douse! Damage x1.5");
                newDamage = (int) (newDamage * 1.5);
                newDamage = Math.round(newDamage * (1 + target.getEffectiveHiddenParam(0)));
                target.requestPopup("Doused!", Color.valueOf("82abcd"));
                break;
            case EVAPORATE:
                System.out.println("Evaporate! Damage x1.5");
                newDamage = (int) (newDamage * 1.5);
                newDamage = Math.round(newDamage * (1 + target.getEffectiveHiddenParam(0)));
                target.requestPopup("Evaporate!", Color.CYAN);
                break;
            case SMOTHERED:
                System.out.println("Smothered! Target's ATK lowered by 50%!");
                reactionSmothered(target, source, source2);
                break;
            case OVERLOAD:
                // TODO: How do I make an AOE Explosion?
                System.out.println("Overload! AoE explosion occurs!");
                reactionOverload(target, source, source2);
                break;
            case MUDDIED:
                System.out.println("Muddied! Target's SPE lowered by 33%!");
                reactionMuddied(target, source, source2);
                break;
            case ELECTROCUTE:
                System.out.println("Electrocuted! Afflicted with [Electrocute] DoT!");
                // Determine which source is primary
                if (markElement == Elements.LIGHTNING) {
                    primarysource = source2;
                    secondarySource = source;
                }
                reactionElectrocute(target, primarysource, secondarySource);
                break;
            case BURN:
                System.out.println("Burned! Afflicted with [Burn] DoT!");
                // Determine which source is primary
                if (markElement == Elements.FIRE) {
                    primarysource = source2;
                    secondarySource = source;
                }
                reactionBurn(target, primarysource, secondarySource);
                break;
            case DAMP:
                System.out.println("Damp! Target's DEF lowered by 33%!");
                reactionDamp(target, source, source2);
                break;
            case ISOLATE:
                System.out.println("Isolate! Target's stunned for 3 turns!");
                reactionIsolate(target, source, source2);
                break;
            default:
                System.out.println("No Reaction Triggered!");
                break;
        }

        return newDamage;
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

    private static int calcReactionDMG(Enemy target, Actor user1, Actor user2, int reactionConstant) {
        int level1 = user1.getLevel();
        int level2 = user2.getLevel();
        int mat1 = user1.getEffectivePrimaryParam(2);
        int mat2 = user2.getEffectivePrimaryParam(2);
        int mdf = target.getEffectivePrimaryParam(1);

        System.out.println("Total Enemy HP: " + target.getMaxHp());

        double reactionDMG = (((double) (level1 + level2) / 5 + 2) * reactionConstant * ((double) (mat1 + mat2) / mdf) * 0.02 + 2);
        System.out.println("Reaction Damage Before Modifier: " + reactionDMG);
        reactionDMG = Math.round(reactionDMG * (1 + target.getEffectiveHiddenParam(0)));
        System.out.println("Reaction Damage After Modifier: " + reactionDMG);
        return ((int) reactionDMG);
    }

    // Reaction Functions
    private static void reactionElectrocute(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 30;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_electrocute", target, reactionDMG);
        target.addStatusEffect("electrocuted", user1);
    }

    private static void reactionOverload(Enemy primaryTarget, Actor user1, Actor user2) {
        int reactionConstant = 40;
        int aoeDamage = calcReactionDMG(primaryTarget, user1, user2, reactionConstant);
        user1.requestReaction("reaction_overload", primaryTarget, aoeDamage);
    }

    private static void reactionSmothered(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 10;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_smothered", target, reactionDMG);
        target.addStatusEffect("smothered");
    }

    private static void reactionDamp(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 10;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_damp", target, reactionDMG);
        target.addStatusEffect("damp");
    }

    private static void reactionMuddied(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 10;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_muddied", target, reactionDMG);
        target.addStatusEffect("muddied");
    }

    private static void reactionIsolate(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 10;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_isolate", target, reactionDMG);
        target.addStatusEffect("isolate");
    }

    private static void reactionBurn(Enemy target, Actor user1, Actor user2) {
        int reactionConstant = 5;
        int reactionDMG = calcReactionDMG(target, user1, user2, reactionConstant);
        user1.requestReaction("reaction_burn", target, reactionDMG);
        target.addStatusEffect("burn", user1);
    }

    private static void clean() {
        // Set firstElementWind back to 0.
        sampledElements.clear();
    }
}
