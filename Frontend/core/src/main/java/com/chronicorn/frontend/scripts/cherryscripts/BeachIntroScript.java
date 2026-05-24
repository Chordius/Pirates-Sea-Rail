package com.chronicorn.frontend.scripts.cherryscripts;

import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class BeachIntroScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (!GameSession.getInstance().isSet("L0_INTRO_DONE")) {
            // Zoom In
            events.queue(new CmdZoomCamera(0.7f, 0f));
            // Focus the Camera between the girl and the Sailor. The "real" player char is actually to the right of this center.
            events.queue(new CmdScrollCamera(-1, 0, 1f));
            // Fade in
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 0.5f));

            events.queue(new CmdWait(0.5f));
            events.queue(new CmdMoveEntity("NPC_Player", 5, 0, true));
            events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));
            events.queue(new CmdWait(0.5f));

            events.queue(new CmdShowBalloon("NPC_Player", "QUESTION", true));
            events.queue(new CmdWait(0.5f));

            events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
            events.queue(new CmdShowText(
                "Girl",
                "Oh, you're finally awake!\\!\nCan you speak? Hello?\\!\nAre you hurt?"
            ));
            events.queue(new CmdWait(0.2f));

            events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
            events.queue(new CmdMoveEntity("NPC_Girl", 5, 0, true));
            events.queue(new CmdShowText(
                "Girl",
                "The others will come in just a second!\\!\nHold on, alright?"
            ));

            events.queue(new CmdWait(0.2f));
            events.queue(new CmdShowBalloon("NPC_Player", "SILENCE", true));
            events.queue(new CmdWait(0.2f));
            events.queue(new CmdMoveEntity("NPC_Player", 4, 0, true));
            events.queue(new CmdMoveEntity("NPC_Girl", 7, 0, true));
            events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));
            events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
            events.queue(new CmdMoveEntity("NPC_Girl", 3, 1, false));
            events.queue(new CmdWait(0.3f));

            // Fade Out
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 0.5f));
            // Change Locatino
            events.queue(new CmdTransferPlayer("CherryTownInnRoom", 6, 5));
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
