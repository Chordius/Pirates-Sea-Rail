package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class Level1Script implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        events.queue(new CmdWindowFlex()
            .setText("Walk with WASD")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                Gdx.graphics.getHeight() * 3 / 4,
                Gdx.graphics.getWidth() * 2 / 3,
                36 + 18
            )
            .setDuration(1f)
            .setBlocking(false)
        );
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {;

        if (triggerName.equals("Gate")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Level2-48"));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }
    }
}
