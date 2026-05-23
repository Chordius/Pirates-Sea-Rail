package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdScrollCamera implements EventCommand {
    private OrthographicCamera camera;
    private float targetX, targetY;
    private float speed; // 0.0 to 1.0 (Lerp factor)
    private boolean finished = false;
    private boolean resetToPlayer = false;

    public CmdScrollCamera(float x, float y, float speed) {
        this.targetX = x;
        this.targetY = y;
        this.speed = speed;
        this.resetToPlayer = false;
    }

    public CmdScrollCamera(boolean resetToPlayer, float speed) {
        this.resetToPlayer = resetToPlayer;
        this.speed = speed;
    }

    @Override
    public void start() {
        if (LevelMapManager.getInstance().getMapScreen() != null) {
            this.camera = LevelMapManager.getInstance().getMapScreen().getCamera();
            
            // Turn off automatic player-following camera updates during manual scroll
            LevelMapManager.getInstance().getMapScreen().setCameraFollow(false);
        }

        if (resetToPlayer && LevelMapManager.getInstance().getPlayer() != null) {
            this.targetX = LevelMapManager.getInstance().getPlayer().getPosition().x;
            this.targetY = LevelMapManager.getInstance().getPlayer().getPosition().y;
        }
    }

    @Override
    public void update(float delta) {
        if (camera == null) {
            finished = true;
            return;
        }

        // Continually track the player position if resetting back to them
        if (resetToPlayer && LevelMapManager.getInstance().getPlayer() != null) {
            this.targetX = LevelMapManager.getInstance().getPlayer().getPosition().x;
            this.targetY = LevelMapManager.getInstance().getPlayer().getPosition().y;
        }

        // Smooth Lerp
        camera.position.x += (targetX - camera.position.x) * speed * delta * 60; // 60 for normalization
        camera.position.y += (targetY - camera.position.y) * speed * delta * 60;
        camera.update();

        // Check if close enough to stop
        if (Math.abs(camera.position.x - targetX) < 1f && Math.abs(camera.position.y - targetY) < 1f) {
            camera.position.x = targetX;
            camera.position.y = targetY;
            camera.update();
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        if (finished) {
            // Restore automatic follow if we've successfully returned to the player
            if (resetToPlayer && LevelMapManager.getInstance().getMapScreen() != null) {
                LevelMapManager.getInstance().getMapScreen().setCameraFollow(true);
            }
            return true;
        }
        return false;
    }
}
