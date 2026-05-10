package com.chronicorn.frontend.managers.eventManagers;

import com.chronicorn.frontend.scripts.*;
// import com.chronicorn.frontend.scripts.BossLevelScript;

public class ScriptRegistry {

    public static MapScript getScriptForMap(String mapName) {
        switch (mapName) {
            case "LevelIntro": return new Level0Script();
            case "Level1": return new Level1Script();
            case "Level2-48": return new Level2Script();
            case "LevelBoss": return new LevelBossScript();
            case "test": return new TestScript();
            default: return new MapScript() {
                @Override public void onMapLoad(EventManager e) {}
                @Override public void onTrigger(String t, EventManager e) {}
            };
        }
    }
}
