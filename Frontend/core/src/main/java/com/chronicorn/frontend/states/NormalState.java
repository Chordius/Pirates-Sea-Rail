package com.chronicorn.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.contants.Direction;

public class NormalState implements PlayerState {

    @Override
    public void handleInput(Player player) {
        if (player.isBusy) {
            player.getVelocity().set(0, 0);
            return;
        }

        float dx = 0;
        float dy = 0;
        boolean keyPressed = false;
        Direction newDir = player.getCurrentDirection();

        // Cek Input WASD
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { newDir = Direction.LEFT; dx = -1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { newDir = Direction.RIGHT; dx = 1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { newDir = Direction.UP; dy = 1; keyPressed = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { newDir = Direction.DOWN; dy = -1; keyPressed = true; }

        if (keyPressed) {
            // --- NEW: Check if the player wants to run ---
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                player.changeState(new DashingState());
                player.handleInput(); // Let DashingState handle this frame's speed
                return;
            }

            player.setCurrentDirection(newDir);

            if (dx != 0 && dy != 0) {
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                dx /= length;
                dy /= length;
            }

            player.getVelocity().set(dx * player.getMaxSpeed(), dy * player.getMaxSpeed());
        } else {
            player.getVelocity().set(0, 0);
        }
    }

    @Override
    public void update(Player player, float delta) {
        // Karena kita menggunakan pergerakan instan di handleInput,
        // kita tidak perlu memanggil applyFriction() atau limitSpeed() di sini.
    }

    @Override
    public void onDashCommand(Player player) {
        if (player.isDashReady()) { // You might need to change this if isDashReady() is removed
            player.changeState(new DashingState());
        }
    }
}
