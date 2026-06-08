package com.chronicorn.frontend.scripts.jailscripts;

import com.badlogic.gdx.Game;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.parties.Deal;
import com.chronicorn.frontend.battlers.parties.Reyna;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail2BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (GameSession.getInstance().isSet("FIRST_TIME_JAIL2B")) {
            events.queue(new CmdPlaySFX("horse_walk1.wav"));
            events.queue(new CmdWait(7f));
            events.queue(new CmdZoomCamera(0.7f, 1f));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", true));
            events.queue(new CmdShowText(
                "Porter",
                "Woah, where are we at?"
            ));
            events.queue(new CmdShowText(
                "Porter",
                "I've never seen this place before in my usual prison breaks..."
            ));
            events.queue(new CmdMoveEntity("Player", "UP", 6, true));
            events.queue(new CmdShowBalloon("Player", "QUESTION", false));
            events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
            events.queue(new CmdWait(0.4f));
            events.queue(new CmdMoveEntity("Player", "TURN_RIGHT", 0, true));
            events.queue(new CmdWait(0.4f));
            events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", true));
            events.queue(new CmdScrollCamera(0, 12, 0.1f, true));
            events.queue(new CmdShowText(
                "Porter",
                "You see that, Sailor?\\! That's a monster alright, we've gotta be careful."
            ));
            events.queue(new CmdScrollCamera(true, 0.1f, true));
            events.queue(new CmdShowText(
                "Porter",
                "When things look dire.\\! Let's not forget to use our potions, alright?"
            ));
            events.queue(new CmdZoomCamera(1f, 0.1f));
        }
        events.queue(new CmdFlagSet("FIRST_TIME_JAIL2B", false));
        events.queue(new CmdSaveGame());
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Cheat_Corner")) {
            GameSession.getInstance().getParty().unlockCharacter("C003", new Reyna());
            GameSession.getInstance().getParty().unlockCharacter("C004", new Deal());
            for (int i = 0; i < 4; i++) {
                Actor selectedActor = GameSession.getInstance().getParty().getActivePartyActors().get(i);
                System.out.println(selectedActor.getName());
                selectedActor.energyChange(100);
            }
        }
    }
}
