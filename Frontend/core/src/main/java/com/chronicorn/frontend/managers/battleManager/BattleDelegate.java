package com.chronicorn.frontend.managers.battleManager;
import com.chronicorn.frontend.battlers.Battler;

public interface BattleDelegate {
    void onFollowUpRequested(Battler source, String skillId, Battler primaryTarget);
    void onReactionRequested(String reactionSkillId, Battler centerTarget, int flatDamage);
}
