package com.chronicorn.frontend.scripts.jailscripts;

import com.chronicorn.frontend.eventcommands.*;
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
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 0.75f));

            events.queue(new CmdScrollCamera(0, -2, 0.1f));
            events.queue(new CmdShowText(
                "Officer",
                "And stay in there, criminal!"
            ));

            events.queue(new CmdShowBalloon("Player", "WORRIED", false));
            events.queue(new CmdMoveEntity("NPC_Guard", "LEFT", 5, true));

        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Chest_Cell_1")) {
            if (!GameSession.getInstance().isSet("GOT_")) {
                GameSession.getInstance().set("GOT_CHEST1_LOOT");

                // Queue commands to add items to player inventory and display dialogue
                events.queue(new CmdAddItem("ITEM_POTION", 2)); // Adds 2 basic potions
            }
        }
    }
}
