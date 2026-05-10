package com.chronicorn.frontend.statuseffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.skills.Skill;

import java.util.HashMap;

public class StatusEffectDatabase {
    private static HashMap<String, JsonValue> statusDataCache = new HashMap<>();

    public static void init() {
        StatusEffectRegistry.init();

        try {
            JsonValue root = new JsonReader().parse(Gdx.files.internal("data/status.json"));

            for (JsonValue entry : root) {
                statusDataCache.put(entry.name, entry);
            }

        } catch (Exception e) {
            Gdx.app.error("StatusDatabase", "Failed to load status.json");
        }
    }

    public static StatusEffect get(String statusId) {
        JsonValue data = statusDataCache.get(statusId);
        if (data != null) {
            StatusEffect status = new StatusEffect(statusId, data);
            if (status.getLogic() != null) {
                return status;
            }
        }
        return null;
    }
}
