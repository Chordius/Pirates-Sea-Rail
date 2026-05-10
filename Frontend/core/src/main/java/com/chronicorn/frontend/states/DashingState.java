package com.chronicorn.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.contants.Direction;

public class DashingState implements PlayerState {

    private final float RUN_MULTIPLIER = 1.6f; // Runs 60% faster than walking

    @Override
    public void handleInput(Player player) {
        if (player.isBusy) {
            player.getVelocity().set(0, 0);
            player.changeState(new NormalState());
            return;
        }

        // If player lets go of Shift, transition back to walking
        if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            player.changeState(new NormalState());
            player.handleInput(); // Immediately calculate the walk logic for this frame
            return;
        }

        float dx = 0;
        float dy = 0;
        boolean keyPressed = false;
        Direction newDir = player.getCurrentDirection();

        // Check WASD Input
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { newDir = Direction.LEFT; dx = -1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { newDir = Direction.RIGHT; dx = 1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { newDir = Direction.UP; dy = 1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { newDir = Direction.DOWN; dy = -1; keyPressed = true; }

        if (keyPressed) {
            player.setCurrentDirection(newDir);

            // NORMALIZATION for diagonal running
            if (dx != 0 && dy != 0) {
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                dx /= length;
                dy /= length;
            }

            float runSpeed = player.getMaxSpeed() * RUN_MULTIPLIER;
            player.getVelocity().set(dx * runSpeed, dy * runSpeed);
        } else {
            // If they are holding shift but released the movement keys, stop and return to normal
            player.getVelocity().set(0, 0);
            player.changeState(new NormalState());
        }
    }

    @Override
    public void update(Player player, float delta) {
        // Optional: You can spawn your Ghost trail here if you still want it while running!
    }

    @Override
    public void onDashCommand(Player player) {
        // Already running
    }
}
