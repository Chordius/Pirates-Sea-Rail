package com.chronicorn.frontend.statuseffect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;
import com.chronicorn.frontend.skills.SkillLogicRegistry;

import java.util.HashMap;

public class StatusEffect {
    private String id;
    private String name;
    private String iconId;
    private int turns;
    private StatusLogic logic;
    private String turnType;
    private String description;
    private HashMap<String, Float> savedValue;
    private Battler origin;

    public StatusEffect(String id, JsonValue data) {
        this.id = id;
        this.name = data.getString("name", "Bad State");
        this.iconId = data.getString("iconId", "default_icon");
        this.turns = data.getInt("turns", 5);
        this.description = data.getString("description", "Lorem Ipsum");
        this.turnType = data.getString("turnType", "turn");
        this.logic = StatusEffectRegistry.getLogic(id);
        this.savedValue = new HashMap<>();
    }

    public String getId() { return id; }
    public int getTurns() { return turns; }
    public StatusLogic getLogic() { return logic; }
    public String getIconId() { return iconId; }
    public String getName() { return name; }
    public String getTurnType() { return turnType; }
    public void setSavedValue(String key, float value) {
        this.savedValue.put(key, value);
    }
    public void setSavedValue(String key, int value) {
        float newValue = (float) value;
        this.savedValue.put(key, newValue);
    }
    public float getSavedValue(String key) {
        return this.savedValue.get(key);
    }
    public void setOrigin(Battler origin) {
        this.origin = origin;
    }
    public Battler getOrigin() {
        return origin;
    }
    public void setTurns(int value) {
        this.turns = value;
    }

    public void decrementTurn() {
        if (turns > 0) turns--;
    }
}
