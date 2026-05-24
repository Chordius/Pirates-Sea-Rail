package com.chronicorn.frontend.battlers;

import com.chronicorn.frontend.managers.battleManager.BattleManager;

public interface EnemyAILogic {
    /**
     * Executes the custom AI behavior for an enemy.
     * @param enemy The enemy instance.
     * @param manager The active BattleManager instance.
     */
    void execute(Enemy enemy, BattleManager manager);
}
