package com.chronicorn.frontend.scripts.jailscripts;

import com.chronicorn.frontend.eventcommands.CmdFade;
import com.chronicorn.frontend.eventcommands.CmdScrollCamera;
import com.chronicorn.frontend.eventcommands.CmdShowText;
import com.chronicorn.frontend.eventcommands.CmdZoomCamera;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail1BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (GameSession.getInstance().isSet("PRISON_CUTSCENE")) {
            events.queue(new CmdZoomCamera(1f, 1f));
            events.queue(new CmdScrollCamera(true, 1f));
            events.queue(new CmdZoomCamera(0.7f, 1f));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 1f));

            events.queue(new CmdScrollCamera(0, -2, 0.1f));
            events.queue(new CmdShowText(
                "And stay in there, criminal!"
            ));
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Chest1")) {
            if (!GameSession.getInstance().isSet("GOT_POTION")) {
                GameSession.getInstance().set("GOT_POTION");
            }
        }
    }
}
