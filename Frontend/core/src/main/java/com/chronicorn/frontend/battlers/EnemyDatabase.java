package com.chronicorn.frontend.battlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;

public class EnemyDatabase {
    private static HashMap<String, JsonValue> enemyDataCache = new HashMap<>();

    public static void init() {
        EnemyAIRegistry.init();

        try {
            JsonValue root = new JsonReader().parse(Gdx.files.internal("data/enemies.json"));

            for (JsonValue entry : root) {
                // Store using lowercase ID for case-insensitive lookup
                enemyDataCache.put(entry.name.toLowerCase(), entry);
            }
        } catch (Exception e) {
            Gdx.app.error("EnemyDatabase", "Failed to load enemies.json", e);
        }
    }

    public static Enemy get(String enemyId) {
        if (enemyId == null) return null;
        JsonValue data = enemyDataCache.get(enemyId.toLowerCase());
        if (data != null) {
            return new Enemy(enemyId.toLowerCase(), data);
        }
        return null;
    }
}
