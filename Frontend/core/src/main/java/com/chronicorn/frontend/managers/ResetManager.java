package com.chronicorn.frontend.managers;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.screens.MapScreen;

public class ResetManager {
    private static final ResetManager instance = new ResetManager();
    private final int DEFAULT_ROOM_TIME = 6;
    private float totalPlayTime = 0;

    private ResetManager() {}

    public static ResetManager getInstance() {
        return instance;
    }
    public void update(float delta) {
        totalPlayTime += delta;
    }
    public int getCurrentScore() {
        return (int) totalPlayTime;
    }
    public void resetGameTimer() {
        totalPlayTime = 0;
        System.out.println("Speedrun Timer Reset to 0.");
    }

    private void forceRespawnEnemies() {
        MapScreen screen = LevelMapManager.getInstance().getMapScreen();
    }
    private void clearEvents() {
        EventManager em = LevelMapManager.getInstance().getEventManager();
        if (em != null) {
            em.clear();
        }
    }

    public void gameOverReset(Player player) {
        // Reset Logic Game
        GameSession.getInstance().reset();

        // Reset Timer Speedrun karena game over
        resetGameTimer();

        String currentMap = LevelMapManager.getInstance().getCurrentMapName();
        SoundManager.getInstance().stopAllAudio();
        LevelMapManager.getInstance().changeLevel("Level1");
        forceRespawnEnemies();
        resetRoomTimer();
        LevelMapManager.getInstance().spawnPlayer();
        MapScreen currentMapScreen = LevelMapManager.getInstance().getMapScreen();
        currentMapScreen.resetTrigger();
    }

    public void restartLevel(Player player) {
        System.out.println("RESTART LEVEL");

        GameSession.getInstance().restoreSnapshot();
        String currentMap = LevelMapManager.getInstance().getCurrentMapName();
        SoundManager.getInstance().stopAllAudio();
        LevelMapManager.getInstance().changeLevel(currentMap);
        LevelMapManager.getInstance().spawnPlayer();
        forceRespawnEnemies();
        resetRoomTimer();
        MapScreen currentMapScreen = LevelMapManager.getInstance().getMapScreen();
        currentMapScreen.resetTrigger();
    }

    public void countdownZero(Player player) {
        System.out.println("TIME'S UP!");
        // Gua gatau caranya
        String currentMap = LevelMapManager.getInstance().getCurrentMapName();
        SoundManager.getInstance().stopAllAudio();
        LevelMapManager.getInstance().spawnPlayer();
        forceRespawnEnemies();
        resetRoomTimer();
        ResetManager.getInstance().resetRoomTimer();
        MapScreen currentMapScreen = LevelMapManager.getInstance().getMapScreen();
        currentMapScreen.resetTrigger();
    }

    public void resetRoomTimer() {
        GameSession.getInstance().setVar("ROOM_COUNTDOWN", DEFAULT_ROOM_TIME);
        System.out.println("Countdown Reset to " + DEFAULT_ROOM_TIME);
    }
}
