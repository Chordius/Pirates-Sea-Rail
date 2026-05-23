package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class BeachIntroScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 0.5f));

        events.queue(new CmdWait(0.5f));
        events.queue(new CmdMoveEntity("NPC_Player", 5, 0, true));
        events.queue(new CmdWait(0.5f));

        events.queue(new CmdShowBalloon("NPC_Player", "QUESTION", true));
        events.queue(new CmdWait(0.5f));

        events.queue(new CmdShowText(
            "Boy",
            "You're finally awake!\\!\nCan you speak, sir? Hello?"
        ));

        events.queue(new CmdShowText(
            "Boy",
            "The adults will come in just a second!\\!\nHold on, alright?"
        ));

        events.queue(new CmdWait(0.2f));
        events.queue(new CmdShowBalloon("NPC_Player", "SILENCE", true));
        events.queue(new CmdWait(0.2f));
        events.queue(new CmdMoveEntity("NPC_Player", 4, 0, true));
        events.queue(new CmdWait(0.2f));

        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 0.5f));
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
