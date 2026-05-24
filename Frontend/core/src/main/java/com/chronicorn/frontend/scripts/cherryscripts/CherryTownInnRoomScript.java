package com.chronicorn.frontend.scripts.cherryscripts;

import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class CherryTownInnRoomScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (!GameSession.getInstance().isSet("L0_INTRO_DONE")) {
            events.queue(new CmdWait(2f));
            events.queue(new CmdScrollCamera(true, 1));
            events.queue(new CmdScrollCamera(-1, 1, 1f));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 0.5f));

            events.queue(new CmdWait(0.5f));
            events.queue(new CmdMoveEntity("NPC_Player", 5, 0, true));
            events.queue(new CmdWait(0.3f));
            events.queue(new CmdShowBalloon("NPC_Player", "SILENCE", true));
            events.queue(new CmdWait(0.3f));
            events.queue(new CmdMoveEntity("NPC_Player", 4, 0, true));
            events.queue(new CmdWait(0.8f));
            events.queue(new CmdMoveEntity("NPC_Player", 6, 0, true));
            events.queue(new CmdWait(0.8f));
            events.queue(new CmdMoveEntity("NPC_Player", 7, 0, true));
            events.queue(new CmdWait(1.2f));

            events.queue(new CmdSetEventSprite("NPC_Player", "feschar_big_001.png", 3));
            events.queue(new CmdShowBalloon("NPC_Player", "QUESTION", false));
            events.queue(new CmdMoveEntity("NPC_Player", 5, 0, true));
            events.queue(new CmdWait(0.8f));
            events.queue(new CmdShowBalloon("NPC_Player", "QUESTION", false));
            events.queue(new CmdMoveEntity("NPC_Player", 6, 0, true));
            events.queue(new CmdWait(1.1f));

            events.queue(new CmdShowText(
                "So, you're saying a traveler is in this room right now!?"
            ));

            events.queue(new CmdMoveEntity("NPC_Player", 4, 0, true));
            events.queue(new CmdWait(1f));
            events.queue(new CmdScrollCamera(2, -4, 0.1f));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
            events.queue(new CmdSetEventLocation("NPC_Boy", 7, 9, 7));
            events.queue(new CmdMoveEntity("NPC_Boy", 3, 2, true));
            events.queue(new CmdScrollCamera(0, 1, 0.1f));

            events.queue(new CmdShowText(
                "Boy",
                "Heellooo!!"
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
            events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", true));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
            events.queue(new CmdShowText(
                "Boy",
                "WOAH!"
            ));
            events.queue(new CmdScrollCamera(-2, 2, 0.1f));
            events.queue(new CmdMoveEntity("NPC_Boy", 1, 2, true));
            events.queue(new CmdMoveEntity("NPC_Boy", 3, 1, true));
            events.queue(new CmdShowText(
                "Boy",
                "A real traveler!\\!\nCool robes and all!"
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
            events.queue(new CmdShowText(
                "Boy",
                "Hi, mister!\\! What's your name?\\!\nMine's Perry!"
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
            events.queue(new CmdShowText(
                "Perry",
                "They say you were sleeping and I shouldn't disturb you.\\!\nBut you look just fine in my eyes."
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
            events.queue(new CmdShowText(
                "Perry",
                "Are there any stories you can share?\\! Have you seen a Cyclops?\\! Calypso?"
            ));
            events.queue(new CmdShowBalloon("NPC_Player", "WORRIED", true));
            events.queue(new CmdShowText(
                "Perry",
                "Another time?\\! You're too tired?\\!\nAww, shucks.\\! Fine."
            ));
            events.queue(new CmdWait(0.25f));
            events.queue(new CmdMoveEntity("NPC_Boy", 5, 0, true));
            events.queue(new CmdWait(0.25f));
            events.queue(new CmdShowText(
                "Perry",
                "You know, my sister was the one that found you.\\!" +
                    "\nFirst time I'd seen her be worried sick like that."
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
            events.queue(new CmdMoveEntity("NPC_Boy", 7, 0, true));
            events.queue(new CmdShowText(
                "Perry",
                "Since you're good to go, why don't you come see her?\\!\nAnd thank everyone that carried you here too."
            ));
            events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
            events.queue(new CmdShowText(
                "Perry",
                "Come on! She's in the diner, just to the north.\\!\nLast one there is a loser!"
            ));

            events.queue(new CmdMoveEntity("NPC_Boy", 2, 2, true));
            events.queue(new CmdMoveEntity("NPC_Boy", 0, 3, true));
            events.queue(new CmdSetEventLocation("NPC_Boy", 16, 12));
            events.queue(new CmdWait(1f));

            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1f));
            events.queue(new CmdSetPlayerTransparency(false));
            events.queue(new CmdWait(1f));
            events.queue(new CmdZoomCamera(1));
            events.queue(new CmdScrollCamera(true, 1f));
            events.queue(new CmdFlagSet("L0_INTRO_DONE", true));
            events.queue(new CmdFlagSet("DINER_SCENE", true));
            events.queue(new CmdFlagSet("AUTOSAVE_ENABLED", true));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 1f));
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Exit")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTownInn", 14, 8));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }
    }
}
