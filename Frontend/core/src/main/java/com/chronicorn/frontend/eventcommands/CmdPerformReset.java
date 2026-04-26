package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.Gdx;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdPerformReset implements EventCommand {
    private Player player;
    private EventManager events;
    private boolean isFinished = false;

    public CmdPerformReset(Player player, EventManager events) {
        this.player = player;
        this.events = events;
    }

    @Override
    public void start() {
        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 100.0f));
        events.queue(new CmdWindowFlex()
            .setText("The souls have departed\nGame Over...")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1/3,
                Gdx.graphics.getHeight() * 1/2 - (36 * 3 + 18) / 2,
                Gdx.graphics.getWidth() * 2/3,
                36 * 2 + 18
            )
            .setDuration(5f)
            .setBlocking(true)
            .setTransparent(true)
        );

        events.queue(new EventCommand() {
            @Override
            public void start() {
                SoundManager.getInstance().stopAllAudio();
                ResetManager.getInstance().gameOverReset(player);
                isFinished = true;
            }

            @Override
            public void update(float delta) {

            }

            @Override
            public boolean isFinished() {
                return isFinished;
            }
        });

        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 1.0f));
    }

    @Override
    public void update(float delta) {
        isFinished = true;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
