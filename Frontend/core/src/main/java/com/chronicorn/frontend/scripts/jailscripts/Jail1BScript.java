package com.chronicorn.frontend.scripts.jailscripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail1BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (GameSession.getInstance().isSet("PRISON_CUTSCENE")) {
            events.queue(new CmdSetBaseSpeed("Player", 100f));
            events.queue(new CmdZoomCamera(1f, 1f));
            events.queue(new CmdScrollCamera(true, 1f));
            events.queue(new CmdZoomCamera(0.7f, 1f));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 0.75f));

            events.queue(new CmdScrollCamera(0, -2, 0.1f));
            events.queue(new CmdShowText(
                "Officer",
                "And stay in there, criminal!"
            ));

            events.queue(new CmdScrollCamera(0, 2, 0.1f, false));
            events.queue(new CmdShowBalloon("Player", "WORRIED", false));
            events.queue(new CmdMoveEntity("NPC_Guard", "LEFT", 12, true));
            events.queue(new CmdShowText(
                "Porter",
                "Ahh, nostalgia..."
            ));
            events.queue(new CmdScrollCamera(-3, 0, 0.1f, true));
            events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
            events.queue(new CmdShowText(
                "Porter",
                "When WAS the last time I got sent to prison?\\! My mind's foggy with all this slow living..."
            ));
            events.queue(new CmdMoveEntity("Porter", "TURN_RIGHT", 0, true));
            events.queue(new CmdShowText(
                "Porter",
                "How are you holding up there, Sailor?\\! Oh, I haven't introduced myself."
            ));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                "Porter",
                "As you've probably heard by now, I'm \\c[YELLOW]Porter Hawks\\c[WHITE].\\! A pirate-turned-handyman.\\! What's your name, buddy?"
            ));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdShowBalloon("Player", "IDEA", true));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                "Porter",
                "Amnesia?\\! You're a funny one.\\! I like you."
            ));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdShowText(
                "Inmate",
                "Can you guys get any more louder?"
            ));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
            events.queue(new CmdShowBalloon("Porter", "SURPRISED", false));
            events.queue(new CmdMoveEntity("Player", "TURN_RIGHT", 0, false));
            events.queue(new CmdMoveEntity("Porter", "TURN_RIGHT", 0, false));
            events.queue(new CmdMoveEntity("Cellmate", "TURN_LEFT", 0, false));
            events.queue(new CmdScrollCamera(7, 0, 0.1f, true));
            events.queue(new CmdShowText(
                "Inmate",
                "We're all trapped in a small cell with nowhere to go.\\! Can't you help keep the peace at least?"
            ));
            events.queue(new CmdScrollCamera(-6, 0, 0.1f, true));
            events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, false));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                "Porter",
                "Yeah, I'm not taking orders from a criminal, buddy."
            ));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdScrollCamera(6, 0, 0.1f, true));
            events.queue(new CmdMoveEntity("Player", "TURN_RIGHT", 0, false));
            events.queue(new CmdShowText(
                "Inmate",
                "But YOU'RE a criminal too, moron."
            ));
            events.queue(new CmdScrollCamera(-6, 0, 0.1f, true));
            events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, false));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
               "Porter",
                "Sailor, this guy is getting annoying. Let's get out of here."
            ));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdShowBalloon("Player", "QUESTION", true));
            events.queue(new CmdMoveEntity("Porter", "TURN_UP", 0, true));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                "Porter",
                "Just give me a couple seconds."
            ));
            events.queue(new CmdMoveEntity("Porter", "DOWN", 1, false));
            events.queue(new CmdMoveEntity("Porter", "TURN_UP", 0, true));
            events.queue(new CmdWait(0.3f));
            events.queue(new CmdSetBaseSpeed("Porter", 250f));
            events.queue(new CmdMoveEntity("Porter", "UP", 2, true));
            events.queue(new CmdShowAnimation("Target_1", "hit-fire", false));
            events.queue(new CmdFlagSet("PORTER_JAILBREAK_1", true));
            events.queue(new CmdScrollCamera(true, 0.1f));
            events.queue(new CmdShowBalloon("Player", "QUESTION", true));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdWait(0.5f));
            events.queue(new CmdSetEventLocation("Porter",46, 0, 0));
            events.queue(new CmdWait(0.7f));
            events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
            events.queue(new CmdWait(0.7f));
            events.queue(new CmdShowAnimation("Target_2", "hit-fire", false));
            events.queue(new CmdFlagSet("PORTER_JAILBREAK_2", true));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", true));
            events.queue(new CmdWait(0.7f));
            events.queue(new CmdSetEventLocation("Porter", 20, 9, 0));
            events.queue(new CmdWait(0.3f));
            events.queue(new CmdShowText(
                "Porter",
                "Hey, what's up?"
            ));
            events.queue(new CmdShowBalloon("Player", "SILENCE", true));
            events.queue(new CmdMoveEntity("Porter", "TURN_RIGHT", 0, true));
            events.queue(new CmdShowText(
                "Porter",
                "Well, what are you waiting for?\\! Let's get out of here!"
            ));
            events.queue(new CmdMoveEntity("Porter", "TURN_UP", 0, true));
            events.queue(new CmdSetEventLocation("Porter",46, 0, 0));
            events.queue(new CmdShowBalloon("Player", "SILENCE", true));
            events.queue(new CmdFlagSet("FIRST_TIME_JAIL2B", true));

            events.queue(new CmdZoomCamera(1f, 0.1f));
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Gate")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Jail-2B"));
        }
    }
}
