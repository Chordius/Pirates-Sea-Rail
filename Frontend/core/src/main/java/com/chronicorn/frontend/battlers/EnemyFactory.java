package com.chronicorn.frontend.battlers;

public class EnemyFactory {
    /**
     * Instantiates an enemy by their database ID.
     * @param enemyId The database ID of the enemy (e.g. "goblin", "pirate").
     * @return An instance of Enemy, or null if not found.
     */
    public static Enemy createEnemy(String enemyId) {
        return EnemyDatabase.get(enemyId);
    }
}
