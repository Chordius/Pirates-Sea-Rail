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
            events.queue(new CmdScrollCamera(true, 0.1f));
            events.queue(new CmdZoomCamera(1f, 0.1f));
            events.queue(new CmdShowText(
                "Demo Done! \n(For Now D:)")
                .showBackground(true)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...

    }
}
