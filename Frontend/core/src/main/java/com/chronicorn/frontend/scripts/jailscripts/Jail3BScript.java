package com.chronicorn.frontend.scripts.jailscripts;

import com.chronicorn.frontend.eventcommands.CmdFade;
import com.chronicorn.frontend.eventcommands.CmdShowText;
import com.chronicorn.frontend.eventcommands.CmdTransferPlayer;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail3BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Gate2")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Jail-2B", 31, 3));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }

        else if (triggerName.equals("Gate1")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Jail-4B", 14, 15));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }
    }
}
