package com.chronicorn.frontend.battlers;

import java.util.HashMap;

public class EnemyAIRegistry {
    private static HashMap<String, EnemyAILogic> logicMap = new HashMap<>();

    public static void init() {
        // Register custom enemy AI behaviors here if needed in the future.
        // Default AI fallback logic is implemented directly in Enemy.java.
    }

    public static EnemyAILogic getLogic(String enemyId) {
        if (enemyId == null) return null;
        return logicMap.get(enemyId.toLowerCase());
    }

    public static void register(String enemyId, EnemyAILogic logic) {
        logicMap.put(enemyId.toLowerCase(), logic);
    }
}
