package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.ActorFactory;
import com.chronicorn.frontend.battlers.parties.Sailor;
import com.chronicorn.frontend.eventcommands.*; // Ini sudah mencakup CmdToggleHUD
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class TestScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        GameSession.getInstance().resetSession();
        events.queue(
                new CmdAddParty("C001"));
        GameSession.getInstance().inventory.addItem("W001", 1);
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Chest1")) {
            if (!GameSession.getInstance().isSet("GOT_POTION")) {
                GameSession.getInstance().set("GOT_POTION");
            }
        } else if (triggerName.equals("Maid_Give")) {
            events.queue(
                new CmdAddItem("W002", 1)
            );
            events.queue(
                new CmdAddItem("W003", 1)
            );
            events.queue(
                new CmdAddItem("W004", 1)
            );
            events.queue(
                new CmdTransferPlayer("CherryTown")
            );
        } else if (triggerName.equals("Maid_Talk")) {
            events.queue(
                    new CmdShowText(
                            "I am going to move.\n" +
                                    "I love pancakes.\n" +
                                    "How do you like dem apples?"));
            events.queue(
                    new CmdMoveEntity("NPC_Maid", 3, 3f, true));
            events.queue(
                    new CmdShowText("Luffy",
                            "Welcome to Pirates: Sea Rail!\n" +
                                    "The story where dreams come to be!\n" +
                                    "I love squidward!"));
        }
    }
}
