package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.screens.BattleScreen;

public class CmdBattle implements EventCommand {
    private Array<String> enemyIds;
    private boolean playerAdvantage = false;
    private String eventName;

    /**
     * Creates a new battle transition command.
     * @param enemyIds The list of enemy database IDs to face in battle.
     */
    public CmdBattle(Array<String> enemyIds) {
        this.enemyIds = enemyIds;
    }

    /**
     * Creates a new battle transition command with advantage flag.
     * @param enemyIds The list of enemy database IDs to face in battle.
     * @param playerAdvantage True if player touches from side/behind.
     */
    public CmdBattle(Array<String> enemyIds, boolean playerAdvantage) {
        this.enemyIds = enemyIds;
        this.playerAdvantage = playerAdvantage;
    }

    /**
     * Creates a new battle transition command with advantage flag and event name.
     * @param enemyIds The list of enemy database IDs to face in battle.
     * @param playerAdvantage True if player touches from side/behind.
     * @param eventName Name of the MapEvent triggering the battle.
     */
    public CmdBattle(Array<String> enemyIds, boolean playerAdvantage, String eventName) {
        this.enemyIds = enemyIds;
        this.playerAdvantage = playerAdvantage;
        this.eventName = eventName;
    }

    /**
     * Creates a new battle transition command.
     * @param enemyIds Varargs list of enemy database IDs.
     */
    public CmdBattle(String... enemyIds) {
        this.enemyIds = new Array<>(enemyIds);
    }

    private float elapsedTime = 0f;
    private final float duration = 1.0f; // Total transition duration
    private boolean started = false;
    private boolean isFinished = false;
    private float initialZoom = 1.0f;

    @Override
    public void start() {
        com.chronicorn.frontend.screens.MapScreen mapScreen = com.chronicorn.frontend.managers.mapManager.LevelMapManager.getInstance().getMapScreen();
        if (mapScreen != null) {
            initialZoom = mapScreen.getCamera().zoom;
            // Play SFX
            com.chronicorn.frontend.managers.SoundManager.getInstance().playSound("Flash1.wav");
        }
        elapsedTime = 0f;
        started = true;
        isFinished = false;
    }

    @Override
    public void update(float delta) {
        if (!started) return;

        elapsedTime += delta;
        com.chronicorn.frontend.screens.MapScreen mapScreen = com.chronicorn.frontend.managers.mapManager.LevelMapManager.getInstance().getMapScreen();
        
        if (mapScreen != null) {
            // Zoom the camera in onto the player
            float t = elapsedTime / duration;
            if (t > 1.0f) t = 1.0f;
            
            // Camera zoom interpolation: fast zoom in
            mapScreen.getCamera().zoom = initialZoom - (initialZoom - 0.35f) * (float) Math.sin(t * Math.PI / 2.0);
            mapScreen.getCamera().update();

            // Flash & Fade phase
            // Phase 1: White Flash (0.0 to 0.3 seconds)
            // Phase 2: Fade to Black (0.3 to 1.0 seconds)
            if (elapsedTime < 0.3f) {
                // Flash white: rises to 1.0 at 0.15s, then down to 0.0 at 0.3s
                float flashT = elapsedTime / 0.3f;
                mapScreen.fadeColor.set(com.badlogic.gdx.graphics.Color.WHITE);
                mapScreen.fadeAlpha = 1.0f - Math.abs(2.0f * flashT - 1.0f); // triangle wave
            } else {
                // Fade to black: from 0.3s to 1.0s, alpha goes 0.0 to 1.0
                float fadeT = (elapsedTime - 0.3f) / (duration - 0.3f);
                if (fadeT > 1.0f) fadeT = 1.0f;
                mapScreen.fadeColor.set(com.badlogic.gdx.graphics.Color.BLACK);
                mapScreen.fadeAlpha = fadeT;
            }
        }

        if (elapsedTime >= duration) {
            if (mapScreen != null) {
                // Reset map screen settings for when we return
                mapScreen.getCamera().zoom = initialZoom;
                mapScreen.getCamera().update();
                mapScreen.fadeAlpha = 1.0f;
                mapScreen.fadeColor.set(com.badlogic.gdx.graphics.Color.BLACK);
                mapScreen.autoFadeInOnShow = true;
            }
            // Trigger the transition to the battle screen
            SceneManager.getInstance().pushScreen(new BattleScreen(enemyIds, playerAdvantage, eventName));
            isFinished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
