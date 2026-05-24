package com.chronicorn.frontend.scripts.cherryscripts;

import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class CherryTownInnScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Exit")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTown", 59, 48));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }

        else if (triggerName.equals("doorAnimate")) {
            events.queue(new CmdMoveEntity("Door1", 5, 0, true));
            events.queue(new CmdWait(0.1f));
            events.queue(new CmdMoveEntity("Door1", 6, 0, true));
            events.queue(new CmdWait(0.1f));
        }

        else if (triggerName.equals("Door_Room1")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTownInnRoom", 7, 9));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }

        else if (triggerName.equals("Door_Room2")) {
            events.queue(new CmdShowText(
                "Door is locked."
            ));
        }

        else if (triggerName.equals("Man1")) {
            events.queue(new CmdShowText(
                "Man",
                "Ach, so you're the traveler that's found on the beach this morning."
            ));
            events.queue(new CmdShowText(
                "Man",
                "Are you good, son?\\! Take it slow, you'll find life is easy here."
            ));
            events.queue(new CmdShowText(
                "Man",
                "Don't forget to thank the Innkeeper.\\! She let you stay in for free, better be" +
                    "thankful for that!"
            ));
        }

        else if (triggerName.equals("Innkeeper")) {
            events.queue(new CmdShowText(
                "Innkeeper",
                "Oh, you're awake now, sweety?\\! Thank the \\c[YELLOW]Sea\\c[WHITE] for that."
            ));
            events.queue(new CmdShowText(
                "Innkeeper",
                "You know, a lotta people were worried about you. They're probably hanging out at" +
                    "the diner right now. " +
                    "Why don't you meet them?"
            ));
        }
    }
}
