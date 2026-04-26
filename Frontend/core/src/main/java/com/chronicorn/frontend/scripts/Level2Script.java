package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.Gdx;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.NetworkCallback;
import com.chronicorn.frontend.managers.NetworkManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class Level2Script implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        events.queue(new CmdWindowFlex()
            .setText("Crash into Enemies to hurt them!")
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
    public void onTrigger(String triggerName, EventManager events) {
        if (triggerName.equals("Room Change")) {
            events.queue(new CmdDecreaseCountdown());
        }
    }
}
