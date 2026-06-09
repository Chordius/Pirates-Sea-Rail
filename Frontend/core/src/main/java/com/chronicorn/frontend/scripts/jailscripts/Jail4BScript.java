package com.chronicorn.frontend.scripts.jailscripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Jail4BScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (!GameSession.getInstance().isSet("MEET_REYNA")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdZoomCamera(0.7f, 1));
            events.queue(new CmdFlagSet("JAIL4B_PORTER_ON", true));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
            events.queue(new CmdShowBalloon("Porter", "SURPRISED", false));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", true));
            events.queue(new CmdShowText(
                    "Porter",
                    "What the hell..."));
            events.queue(new CmdSetBaseSpeed("Player", 140));
            events.queue(new CmdMoveEntity("Player", "UP", 3, false));
            events.queue(new CmdMoveEntity("Porter", "UP", 3, true));
            events.queue(new CmdScrollCamera(0, 2, 0.1f, true));
            events.queue(new CmdWait(1f));
            events.queue(new CmdScrollCamera(0, -2, 0.1f, true));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                    "Porter",
                    "Isn't that an elf?\\!\\nWhat is an elf doing here?\\nIn the basement of a prison?"));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdScrollCamera(0, 1.5f, 0.1f, true));
            events.queue(new CmdMoveEntity("Reyna", "TURN_RIGHT", 0, true));
            events.queue(new CmdShowBalloon("Reyna", "QUESTION", true));
            events.queue(new CmdShowText(
                    "Elf",
                    "...Humans?\\! What have you come to do this time?"));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                    "Porter",
                    "This time?\\! Sorry lady, I've never even met you."));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdMoveEntity("Reyna", "TURN_LEFT", 0, true));
            events.queue(new CmdShowText(
                    "Elf",
                    "You're here to torture me further, aren't you?\\! Shut your nonsense, make it fast."));
            events.queue(new CmdMoveEntity("Player", "UP", 1, true));
            events.queue(new CmdShowBalloon("Player", "IDEA", true));
            events.queue(new CmdShowText(
                    "Elf",
                    "What am I doing here?\\!\\nYOU'RE the one who trapped me in this chamber!"));
            events.queue(new CmdShowText(
                    "Elf",
                    "I should be asking YOUR motives, Leviathan-worshipping scum!"));
            events.queue(new CmdShowBalloon("Porter", "ANGER", true));
            events.queue(new CmdSetSteppingAnimation("Porter", true));
            events.queue(new CmdShowText(
                    "Porter",
                    "Lady, you're really pissing me off.\\! Come on, Sailor, let's leave her-"));
            events.queue(new CmdSetSteppingAnimation("Porter", false));
            events.queue(new CmdPlaySFX("Monster1.mp3"));
            events.queue(new CmdMoveEntity("Reyna", "TURN_DOWN", 0, false));
            events.queue(new CmdMoveEntity("Porter", "TURN_DOWN", 0, false));
            events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, false));
            events.queue(new CmdShowBalloon("Reyna", "SURPRISED", false));
            events.queue(new CmdShowBalloon("Porter", "SURPRISED", false));
            events.queue(new CmdShowBalloon("Player", "SURPRISED", true));

            events.queue(new CmdShowText(
                    "Porter",
                    "What the-"));
            events.queue(new CmdRecoverAll());
            events.queue(new CmdBattle(
                    "plesiosaurus",
                    "song_boss.mp3"));
            events.queue(new CmdZoomCamera(1f, 0.3f));
            events.queue(new CmdShowText(
                "Demo Finished\\n(For Now)")
                .showBackground(false)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );
            events.queue(new CmdSetBaseSpeed("Player", 100));
            events.queue(new CmdFlagSet("JAIL4B_PORTER_ON", false));
            events.queue(new CmdFlagSet("MEET_REYNA", true));
            events.queue(new CmdSaveGame());
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Gate")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Jail-3B", 23, 5));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }
    }
}
