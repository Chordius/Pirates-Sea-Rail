package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.screens.MapScreen;

public class CmdScrollCamera implements EventCommand {
    private float tileOffsetX = 0f, tileOffsetY = 0f;
    private float speed; // 0.0 to 1.0 (Lerp factor)
    private boolean resetToPlayer = false;
    private boolean isWait = true;
    private boolean started = false;
    private MapScreen mapScreen;

    public CmdScrollCamera(float tileOffsetX, float tileOffsetY, float speed) {
        this(tileOffsetX, tileOffsetY, speed, true);
    }

    public CmdScrollCamera(float tileOffsetX, float tileOffsetY, float speed, boolean isWait) {
        this.tileOffsetX = tileOffsetX;
        this.tileOffsetY = tileOffsetY;
        this.speed = speed;
        this.isWait = isWait;
        this.resetToPlayer = false;
    }

    public CmdScrollCamera(boolean resetToPlayer, float speed) {
        this(resetToPlayer, speed, true);
    }

    public CmdScrollCamera(boolean resetToPlayer, float speed, boolean isWait) {
        this.resetToPlayer = resetToPlayer;
        this.speed = speed;
        this.isWait = isWait;
    }

    @Override
    public void start() {
        this.mapScreen = LevelMapManager.getInstance().getMapScreen();
        if (mapScreen == null) {
            started = true;
            return;
        }

        float targetX, targetY;
        if (resetToPlayer) {
            if (LevelMapManager.getInstance().getPlayer() != null) {
                targetX = LevelMapManager.getInstance().getPlayer().getPosition().x;
                targetY = LevelMapManager.getInstance().getPlayer().getPosition().y;
            } else {
                targetX = mapScreen.getCamera().position.x;
                targetY = mapScreen.getCamera().position.y;
            }
        } else {
            targetX = mapScreen.getCamera().position.x + (tileOffsetX * 48f);
            targetY = mapScreen.getCamera().position.y + (tileOffsetY * 48f);
        }

        mapScreen.setCameraTarget(targetX, targetY, speed, resetToPlayer);
        started = true;
    }

    @Override
    public void update(float delta) {
        // MapScreen updates the camera position in the background
    }

    @Override
    public boolean isFinished() {
        if (!started) return false;
        if (mapScreen == null) return true;

        if (isWait) {
            return !mapScreen.hasCameraTarget();
        }
        return true;
    }
}
