package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdZoomCamera implements EventCommand {
    private OrthographicCamera camera;
    private float targetZoom;
    private float speed;
    private boolean finished = false;

    /**
     * Zooms the camera to targetZoom over time with the specified speed.
     * @param targetZoom The target zoom factor (e.g., 0.5 for 2x zoom in, 1.0 for default, 2.0 for 2x zoom out).
     * @param speed The interpolation speed factor.
     */
    public CmdZoomCamera(float targetZoom, float speed) {
        this.targetZoom = targetZoom;
        this.speed = speed;
    }

    /**
     * Zooms the camera to targetZoom instantly.
     * @param targetZoom The target zoom factor.
     */
    public CmdZoomCamera(float targetZoom) {
        this.targetZoom = targetZoom;
        this.speed = 0f;
    }

    /**
     * Resets the camera zoom back to 1.0f instantly.
     */
    public CmdZoomCamera() {
        this.targetZoom = 1.0f;
        this.speed = 0f;
    }

    @Override
    public void start() {
        if (LevelMapManager.getInstance().getMapScreen() != null) {
            this.camera = LevelMapManager.getInstance().getMapScreen().getCamera();
        }

        if (camera == null) {
            finished = true;
            return;
        }

        if (speed <= 0f) {
            camera.zoom = targetZoom;
            camera.update();
            finished = true;
        }
    }

    @Override
    public void update(float delta) {
        if (camera == null) {
            finished = true;
            return;
        }

        // Smooth Lerp
        camera.zoom += (targetZoom - camera.zoom) * speed * delta * 60f;

        // Check if close enough to stop and snap
        if (Math.abs(camera.zoom - targetZoom) < 0.005f) {
            camera.zoom = targetZoom;
            camera.update();
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
