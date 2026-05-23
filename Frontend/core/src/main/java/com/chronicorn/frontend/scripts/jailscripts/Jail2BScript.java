package com.chronicorn.frontend.scripts.jailscripts;

import com.chronicorn.frontend.eventcommands.CmdShowText;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail2BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        events.queue(
            new CmdShowText(
                "Uhh... is it daytime yet?\n"
            )
        );
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
