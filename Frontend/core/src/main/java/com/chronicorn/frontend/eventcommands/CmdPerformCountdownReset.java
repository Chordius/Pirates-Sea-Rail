package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.Gdx;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdPerformCountdownReset implements EventCommand {
    private Player player;
    private EventManager events;
    private boolean isFinished = false;

    public CmdPerformCountdownReset(Player player, EventManager events) {
        this.player = player;
        this.events = events;
    }

    @Override
    public void start() {
        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1.0f));

        events.queue(new CmdToggleHUD(true));
        events.queue(new EventCommand() {
            @Override
            public void start() {
                // Call the restart logic we made earlier
                ResetManager.getInstance().countdownZero(player);
                isFinished = true;
            }

            @Override
            public void update(float delta) {

            }

            @Override
            public boolean isFinished() {
                return true;
            }
        });

        events.queue(new CmdWait(1));

        // 2. Display "The time has ended..."
        events.queue(new CmdWindowFlex()
            .setText("The time has ended...")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - 200,
                Gdx.graphics.getHeight() / 2 - 50,
                400, 100
            )
            .setDuration(3f)
            .setBlocking(true)
            .setTransparent(true) // Invisible window background
        );

        float[] durations = {0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 1.2f};

        for (int i = 0; i <= 6; i++) {
            events.queue(new CmdWindowFlex()
                .resetContent() // Clear previous text
                .addImage("Numbers/" + i + ".png") // Ensure you have 1.png, 2.png...
                .setBounds(
                    Gdx.graphics.getWidth() / 2 - 480,
                    Gdx.graphics.getHeight() / 2 - 360,
                    960, 720
                )
                .setDuration(durations[i]) // Use our speeding-up array
                .setBlocking(true)
                .setTransparent(true)
            );
            if (i != 6) {
                events.queue(new CmdPlaySFX("Saint5.wav"));
            }
        }

        events.queue(new CmdPlaySFX("Flash1.wav"));
        events.queue(new CmdWait(1));

        events.queue(new EventCommand() {
            @Override
            public void start() {
                // Call the restart logic we made earlier
                LevelMapManager.getInstance().playMusic();
                isFinished = true;
            }

            @Override
            public void update(float delta) {

            }

            @Override
            public boolean isFinished() {
                return true;
            }
        });

        // Fade In
        events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2.0f));
        events.queue(new CmdToggleHUD(false));

        isFinished = true;
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
