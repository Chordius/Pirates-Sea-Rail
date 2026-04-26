package com.chronicorn.frontend.scripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.NetworkCallback;
import com.chronicorn.frontend.managers.NetworkManager;
import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.Gate;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.screens.TitleScreen;

public class LevelBossScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
        if (GameSession.getInstance().isSet("LB_BOSS_DEFEATED")) {
            InteractiveObject gateObj = LevelMapManager.getInstance().getObjectByName("Gate");
            if (gateObj != null && gateObj instanceof Gate) {
                ((Gate) gateObj).open();
            }
        }
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        if (triggerName.equals("Gate")) {

            if (GameSession.getInstance().isSet("LB_BOSS_DEFEATED")) {
                events.queue(new CmdToggleHUD(true));
                Player player = LevelMapManager.getInstance().getPlayer();
                String username = Main.currentUsername; // Ambil username yang login
                int finalTimeScore = ResetManager.getInstance().getCurrentScore(); // Ambil Waktu

                System.out.println("Uploading Score: " + finalTimeScore + " seconds...");

                // Panggil API Backend
                NetworkManager.getInstance().saveGame(
                    username,
                    finalTimeScore,
                    player.getHp(),
                    player.getAtk(),
                    player.getDef(),
                    new NetworkCallback() {
                        @Override
                        public void onSuccess(String response) {
                            System.out.println("SERVER: " + response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            System.err.println("SERVER ERROR: " + errorMessage);
                        }
                    }
                );
                events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 50.0f));
                events.queue(new CmdShowText(
                    "Congratulations!\n" +
                        "You have defeated the Cultist Boss and completed the demo.\n" +
                        "Thank you for playing!" +
                            "\nDon't forget to leave a comment on our itch.io page!"
                ));

                events.queue(new CmdWindowFlex()
                    .setText("Demo Completed")
                    .setBounds(
                        Gdx.graphics.getWidth() / 2 - 150,
                        Gdx.graphics.getHeight() / 2 - 50,
                        300, 100
                    )
                    .setDuration(4f)
                    .setBlocking(true)
                );

                events.queue(new EventCommand() {
                    @Override
                    public void start() {
                        // Kembali ke Menu Utama
                        SceneManager.getInstance().changeScreen(new TitleScreen());
                    }

                    @Override
                    public void update(float delta) {}

                    @Override
                    public boolean isFinished() { return true; }
                });

            } else {
                events.queue(new CmdWindowFlex()
                    .setText("The gate is sealed by a dark magic...\nDefeat the guardian first!")
                    .setBounds(
                        Gdx.graphics.getWidth() / 2 - 200,
                        Gdx.graphics.getHeight() - 150,
                        400, 80
                    )
                    .setDuration(2f)
                    .setBlocking(false)
                );
            }
        }
    }
}
