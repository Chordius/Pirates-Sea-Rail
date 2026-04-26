package com.chronicorn.frontend.managers.battleManager.mechanics;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;

import java.util.ArrayList;

public class TargetingLogic {

    public static IntArray getTargetIndices(int primaryIndex, int totalUnits, TargetScope scope) {
        IntArray indices = new IntArray();

        if (scope == null || totalUnits <= 0) return indices;

        int clampedPrimary = Math.max(0, Math.min(primaryIndex, totalUnits - 1));

        switch (scope) {
            case SINGLE:
            case ALLY:
            case SELF:
                indices.add(clampedPrimary);
                break;

            case BLAST:
                if (clampedPrimary > 0) indices.add(clampedPrimary - 1);
                indices.add(clampedPrimary);
                if (clampedPrimary < totalUnits - 1) indices.add(clampedPrimary + 1);
                break;

            case ALL:
            case ALLIES:
                for (int i = 0; i < totalUnits; i++) {
                    indices.add(i);
                }
                break;
        }
        return indices;
    }

    public static Array<Battler> resolveTargets(Battler user, Battler primaryTarget, Array<Battler> fallbackTargets, ArrayList<Battler> allBattlers, TargetScope scope) {
        Array<Battler> resolved = new Array<>();

        if (scope == null || user == null || allBattlers == null) return resolved;

        Array<Battler> lane = new Array<>();
        boolean isUserPlayer = user.isPlayerControlled();

        // Determine the lane of potential targets based on scope
        if (scope == TargetScope.SELF) {
            lane.add(user);
        }

        if (scope == TargetScope.ALLY || scope == TargetScope.ALLIES) {
            for (Battler b : allBattlers) {
                boolean isBattlerAlly = b.isPlayerControlled() == isUserPlayer;
                if (b.isAlive() && isBattlerAlly) {
                    lane.add(b);
                }
            }
        }

        if (scope == TargetScope.SINGLE || scope == TargetScope.BLAST || scope == TargetScope.ALL) {
            for (Battler b : allBattlers) {
                boolean isBattlerEnemy = b.isPlayerControlled() != isUserPlayer;
                if (b.isAlive() && isBattlerEnemy) {
                    lane.add(b);
                }
            }
        }

        // Edge Case 0: No valid targets in lane. If scope allows self-targeting, default to user.
        if (lane.size == 0) {
            if (scope == TargetScope.SELF || scope == TargetScope.ALLY) {
                resolved.add(user);
            }
            return resolved;
        }

        // Edge Case 1: Primary target not in lane, but fallback targets are. Use the first fallback as primary.
        int primaryIndex = lane.indexOf(primaryTarget, true);
        if (primaryIndex == -1 && fallbackTargets != null && fallbackTargets.size > 0) {
            primaryIndex = lane.indexOf(fallbackTargets.first(), true);
        }

        // Edge Case 2: Primary target not in lane, and no valid fallback. Default to user or first in lane.
        if (primaryIndex == -1) {
            if (scope == TargetScope.ALLY) {
                int userIndex = lane.indexOf(user, true);
                primaryIndex = userIndex != -1 ? userIndex : 0;
            } else {
                primaryIndex = 0;
            }
        }

        // Get the final target indices based on the primary index and scope, then resolve to actual Battler objects
        IntArray targetIndices = getTargetIndices(primaryIndex, lane.size, scope);
        for (int i = 0; i < targetIndices.size; i++) {
            int idx = targetIndices.get(i);
            if (idx >= 0 && idx < lane.size) {
                resolved.add(lane.get(idx));
            }
        }

        // Edge Case 3: No valid targets found, but scope allows self-targeting. Default to user.
        if (resolved.size == 0 && (scope == TargetScope.SELF || scope == TargetScope.ALLY)) {
            resolved.add(user);
        }

        return resolved;
    }
}
