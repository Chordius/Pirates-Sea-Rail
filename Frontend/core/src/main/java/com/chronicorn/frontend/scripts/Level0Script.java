package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.eventcommands.*; // Ini sudah mencakup CmdToggleHUD
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class Level0Script implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        // Intro Cutscene Logic
        if (!GameSession.getInstance().isSet("L0_INTRO_DONE")) {

            // 1. SEMBUNYIKAN HUD (Supaya layar hitam polos)
            events.queue(new CmdToggleHUD(true));

            // Layar langsung hitam (Speed 1000 = Instan)
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1000.0f));

            events.queue(new CmdWait(1f));

            // --- TEXT SEQUENCE ---
            events.queue(new CmdShowText(
                    "Long ago, a hero was born destined to defeat the evil mighty Dark Dragon...",
                    0,
                    Gdx.graphics.getHeight() /2 - 90
                ).setAlignment(Align.top)
            );
            events.queue(new CmdShowText(
                    "However, he is defeated very shortly after.",
                    0,
                    Gdx.graphics.getHeight() /2 - 90
                ).setAlignment(Align.top)
            );
            events.queue(new CmdShowText(
                    "Now, it is his job, Mr. Morning Sunday\nto fulfill the destiny of his rider...",
                    0,
                    Gdx.graphics.getHeight() /2 - 90
                ).setAlignment(Align.top)
            );
            events.queue(new CmdShowText(
                    "And so began, the quest of a horse in time...\n A Chronicorn.",
                    0,
                    Gdx.graphics.getHeight() /2 - 90
                ).setAlignment(Align.top)
            );

            // 2. MUNCULKAN KEMBALI HUD (Sebelum masuk game/level 1)
            events.queue(new CmdToggleHUD(false));

            // Pindah Level & Fade In
            events.queue(new CmdTransferPlayer("Level1"));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 1.0f));

            GameSession.getInstance().set("L0_INTRO_DONE");
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        // Trigger logic lainnya tetap sama...
        if (triggerName.equals("Chest1")) {
            if (!GameSession.getInstance().isSet("GOT_POTION")) {
                GameSession.getInstance().set("GOT_POTION");
            }
        }
        else if (triggerName.equals("Gate")) {
            // ... logic gate tetap sama ...
            events.queue(new CmdWindowFlex()
                .setText("Danger: Zombies Ahead!")
                .setBounds(
                    Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1/3,
                    Gdx.graphics.getHeight() * 4/5,
                    Gdx.graphics.getWidth() * 2/3,
                    36 + 18
                )
                .setDuration(10f)
                .setBlocking(false)
            );
            events.queue(new CmdShowText(
                "- Akbar -\n" +
                    "Hello World!\n" +
                    "I am going to school today."
            ));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer("Level2-48"));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
        }
    }
}
