package com.chronicorn.frontend.scripts.cherryscripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.eventcommands.*; // Ini sudah mencakup CmdToggleHUD
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class Level0Script implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        // Intro Cutscene Logic
        if (!GameSession.getInstance().isSet("L0_INTRO_DONE")) {
            events.queue(new CmdAddParty("C001"));

            // 1. SEMBUNYIKAN HUD (Supaya layar hitam polos)
            events.queue(new CmdToggleHUD(true));

            // Layar langsung hitam (Speed 1000 = Instan)
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1000.0f));

            events.queue(new CmdWait(3f));

            // --- TEXT SEQUENCE ---
            events.queue(new CmdShowText(
                    "...")
                .showBackground(false)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );

            events.queue(new CmdWait(1f));
            events.queue(new CmdSetPlayerTransparency(true));

            events.queue(new CmdShowText(
                    "...E..y...")
                .showBackground(false)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );

            events.queue(new CmdWait(0.3f));

            events.queue(new CmdShowText(
                    "He...y...\\! m...ster...")
                .showBackground(false)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );

            events.queue(new CmdWait(0.5f));

            events.queue(new CmdShowText(
                    "Hey, mister?\\!" +
                            "\nWake up!")
                .showBackground(false)
                .setAlignment(Align.center)
                .setX(((float) Gdx.graphics.getWidth() / 2) - ((float) 1000/2))
                .setY(((float) Gdx.graphics.getHeight() / 2) - ((float) 168/2))
            );

            // Pindah Level & Fade In
            events.queue(new CmdTransferPlayer("Beach-Intro"));
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Chest1")) {
            if (!GameSession.getInstance().isSet("GOT_POTION")) {
                GameSession.getInstance().set("GOT_POTION");
            }
        } else if (triggerName.equals("Gate")) {
            // ... logic gate tetap sama ...
            events.queue(new CmdWindowFlex()
                    .setText("Danger: Zombies Ahead!")
                    .setBounds(
                            Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1 / 3,
                            Gdx.graphics.getHeight() * 4 / 5,
                            Gdx.graphics.getWidth() * 2 / 3,
                            36 + 18)
                    .setDuration(10f)
                    .setBlocking(false));
            events.queue(new CmdShowText(
                    "- Akbar -\n" +
                            "Hello World!\n" +
                            "I am going to school today."));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Level2-48"));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }
    }
}
