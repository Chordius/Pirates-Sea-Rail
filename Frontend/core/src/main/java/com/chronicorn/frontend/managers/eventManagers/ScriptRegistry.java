package com.chronicorn.frontend.managers.eventManagers;

import com.chronicorn.frontend.scripts.Level0Script;
import com.chronicorn.frontend.scripts.Level2Script;
import com.chronicorn.frontend.scripts.LevelBossScript;
import com.chronicorn.frontend.scripts.MapScript;
import com.chronicorn.frontend.scripts.Level1Script;
// import com.chronicorn.frontend.scripts.BossLevelScript;

public class ScriptRegistry {

    public static MapScript getScriptForMap(String mapName) {
        switch (mapName) {
            case "LevelIntro": return new Level0Script();
            case "Level1": return new Level1Script();
            case "Level2-48": return new Level2Script();
            case "LevelBoss": return new LevelBossScript();
            default: return new MapScript() {
                @Override public void onMapLoad(EventManager e) {}
                @Override public void onTrigger(String t, EventManager e) {}
            };
        }
    }
}
