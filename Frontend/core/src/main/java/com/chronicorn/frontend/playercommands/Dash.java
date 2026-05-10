package com.chronicorn.frontend.playercommands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.chronicorn.frontend.Player;

public class Dash implements Command {
    private Player player;

    public Dash(Player player) {
        this.player = player;
    }

    @Override
    public void execute() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (!player.isDead()) {
                player.attemptDash();
            }
        }
    }
}
