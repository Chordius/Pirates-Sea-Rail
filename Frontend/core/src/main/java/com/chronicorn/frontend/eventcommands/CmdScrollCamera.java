package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class CmdScrollCamera implements EventCommand {
    private OrthographicCamera camera;
    private float targetX, targetY;
    private float speed; // 0.0 to 1.0 (Lerp factor)
    private boolean finished = false;

    public CmdScrollCamera(OrthographicCamera cam, float x, float y, float speed) {
        this.camera = cam;
        this.targetX = x;
        this.targetY = y;
        this.speed = speed;
    }

    @Override
    public void start() {}

    @Override
    public void update(float delta) {
        // Smooth Lerp
        camera.position.x += (targetX - camera.position.x) * speed * delta * 60; // 60 for normalization
        camera.position.y += (targetY - camera.position.y) * speed * delta * 60;
        camera.update();

        // Check if close enough to stop
        if (Math.abs(camera.position.x - targetX) < 1f && Math.abs(camera.position.y - targetY) < 1f) {
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
