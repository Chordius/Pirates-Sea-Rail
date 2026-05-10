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

        // If we have no scope, no battlers, or neither a user nor a target to anchor from, abort.
        if (scope == null || allBattlers == null || (user == null && primaryTarget == null)) {
            return resolved;
        }

        Array<Battler> lane = new Array<>();

        // 1. Determine Target Alignment (Player Team vs Enemy Team)
        boolean isTargetPlayerTeam;

        if (user != null) {
            // Standard Action: Target lane depends on the user's alignment and the skill's scope
            boolean isUserPlayer = user.isPlayerControlled();
            if (scope == TargetScope.ALLY || scope == TargetScope.ALLIES || scope == TargetScope.SELF) {
                isTargetPlayerTeam = isUserPlayer;
            } else {
                isTargetPlayerTeam = !isUserPlayer;
            }
        } else {
            // Reaction Action (User is null): The target lane is simply the team of the primary target.
            // For example, if Overload (ALL or BLAST) triggers on an Enemy, the target lane is the Enemy team.
            isTargetPlayerTeam = primaryTarget.isPlayerControlled();
        }

        // 2. Build the Lane
        if (scope == TargetScope.SELF) {
            // If user is null, SELF defaults to the primaryTarget
            lane.add(user != null ? user : primaryTarget);
        } else {
            for (Battler b : allBattlers) {
                if (b.isAlive() && b.isPlayerControlled() == isTargetPlayerTeam) {
                    lane.add(b);
                }
            }
        }

        // 3. Edge Case 0: No valid targets in lane.
        if (lane.size == 0) {
            if ((scope == TargetScope.SELF || scope == TargetScope.ALLY) && user != null) {
                resolved.add(user);
            } else if (primaryTarget != null) {
                resolved.add(primaryTarget);
            }
            return resolved;
        }

        // 4. Find the center index for targeting
        int primaryIndex = lane.indexOf(primaryTarget, true);

        // Edge Case 1: Primary target not in lane, use fallback.
        if (primaryIndex == -1 && fallbackTargets != null && fallbackTargets.size > 0) {
            primaryIndex = lane.indexOf(fallbackTargets.first(), true);
        }

        // Edge Case 2: No valid fallback. Default to user or first in lane.
        if (primaryIndex == -1) {
            if (scope == TargetScope.ALLY && user != null) {
                int userIndex = lane.indexOf(user, true);
                primaryIndex = userIndex != -1 ? userIndex : 0;
            } else {
                primaryIndex = 0;
            }
        }

        // 5. Apply the Scope pattern (Single, Blast, All)
        IntArray targetIndices = getTargetIndices(primaryIndex, lane.size, scope);
        for (int i = 0; i < targetIndices.size; i++) {
            int idx = targetIndices.get(i);
            if (idx >= 0 && idx < lane.size) {
                resolved.add(lane.get(idx));
            }
        }

        // 6. Failsafe
        if (resolved.size == 0) {
            if ((scope == TargetScope.SELF || scope == TargetScope.ALLY) && user != null) {
                resolved.add(user);
            } else if (primaryTarget != null) {
                resolved.add(primaryTarget);
            }
        }

        return resolved;
    }
}
