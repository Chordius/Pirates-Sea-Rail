package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.objects.PhysicsObjects;

public class CmdMoveEntity implements EventCommand {
    private PhysicsObjects entity;
    private Vector2 target;
    private float speed = 100f;

    public CmdMoveEntity(PhysicsObjects entity, float x, float y) {
        this.entity = entity;
        this.target = new Vector2(x, y);
    }

    @Override
    public void start() {
        // Optional: Play walking animation
    }

    @Override
    public void update(float delta) {
        Vector2 pos = entity.getPosition();
        Vector2 direction = new Vector2(target).sub(pos).nor();

        pos.mulAdd(direction, speed * delta);
        entity.getBounds().setPosition(pos.x, pos.y);
    }

    @Override
    public boolean isFinished() {
        return entity.getPosition().dst(target) < 2f; // Stop when within 2 pixels
    }
}
