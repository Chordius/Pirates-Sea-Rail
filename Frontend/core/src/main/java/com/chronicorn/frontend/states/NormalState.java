package com.chronicorn.frontend.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.contants.Direction;

public class NormalState implements PlayerState {
    private boolean isBusy = false;

    @Override
    public void handleInput(Player player) {
        float dx = 0;
        float dy = 0;
        boolean keyPressed = false;
        isBusy = player.isBusy;
        Direction newDir = player.getCurrentDirection();

        // Cek Input WASD
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newDir = Direction.LEFT;
            dx = -1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newDir = Direction.RIGHT;
            dx = 1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            newDir = Direction.UP;
            dy = 1;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newDir = Direction.DOWN;
            dy = -1;
            keyPressed = true;
        }

        if (keyPressed && !isBusy) {
            player.setCurrentDirection(newDir);
            // Langsung set kecepatan ke MaxSpeed (Tanpa akselerasi bertahap)
            player.setExactVelocity(dx * player.getMaxSpeed(), dy * player.getMaxSpeed());
        } else {
            // Jika tidak ada tombol ditekan, langsung berhenti total
            player.setExactVelocity(0, 0);
        }
    }

    @Override
    public void update(Player player, float delta) {
    }

    @Override
    public void onDashCommand(Player player) {
        if (player.isDashReady()) {
            player.changeState(new DashingState());
        }
    }
}
