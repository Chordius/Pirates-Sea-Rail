package com.chronicorn.frontend.managers.eventManagers;

import com.chronicorn.frontend.scripts.*;
import com.chronicorn.frontend.scripts.cherryscripts.*;
import com.chronicorn.frontend.scripts.jailscripts.Jail1BScript;
import com.chronicorn.frontend.scripts.jailscripts.Jail2BScript;
import com.chronicorn.frontend.scripts.jailscripts.Jail3BScript;
import com.chronicorn.frontend.scripts.jailscripts.Jail4BScript;
import com.chronicorn.frontend.scripts.legacyscripts.Level1Script;
import com.chronicorn.frontend.scripts.legacyscripts.Level2Script;
import com.chronicorn.frontend.scripts.legacyscripts.LevelBossScript;
// import com.chronicorn.frontend.scripts.BossLevelScript;

public class ScriptRegistry {

    public static MapScript getScriptForMap(String mapName) {
        switch (mapName) {
            case "LevelIntro": return new Level0Script();
            case "Level1": return new Level1Script();
            case "Level2-48": return new Level2Script();
            case "LevelBoss": return new LevelBossScript();
            case "test": return new TestScript();
            case "Beach-Intro": return new BeachIntroScript();
            case "CherryTown": return new CherryTownScript();
            case "Jail-1B": return new Jail1BScript();
            case "Jail-2B": return  new Jail2BScript();
            case "Jail-3B": return new Jail3BScript();
            case "Jail-4B": return new Jail4BScript();
            case "CherryTownInnRoom": return new CherryTownInnRoomScript();
            case "CherryTownInn": return new CherryTownInnScript();
            case "CherryTownDiner": return new CherryTownDinerScript();
            default: return new MapScript() {
                @Override public void onMapLoad(EventManager e) {}
                @Override public void onTrigger(String t, EventManager e) {}
            };
        }
    }
}
