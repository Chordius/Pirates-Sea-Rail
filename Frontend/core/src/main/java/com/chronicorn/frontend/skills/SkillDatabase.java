package com.chronicorn.frontend.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.skills.Skill;
import com.chronicorn.frontend.skills.SkillLogicRegistry;

import java.util.HashMap;

public class SkillDatabase {
    // Cache the JSON data, not the instantiated Skill objects
    private static HashMap<String, JsonValue> skillDataCache = new HashMap<>();

    public static void init() {
        SkillLogicRegistry.init();

        try {
            JsonValue root = new JsonReader().parse(Gdx.files.internal("data/skills.json"));

            for (JsonValue entry : root) {
                // Store the raw JSON tree in memory
                skillDataCache.put(entry.name, entry);
            }
        } catch (Exception e) {
            Gdx.app.error("SkillDatabase", "Failed to load skills.json", e);
        }
    }

    // Construct a brand new instance every time a character learns a skill
    public static Skill get(String skillId) {
        JsonValue data = skillDataCache.get(skillId);
        if (data != null) {
            return new Skill(skillId, data);
        }
        return null;
    }
}
