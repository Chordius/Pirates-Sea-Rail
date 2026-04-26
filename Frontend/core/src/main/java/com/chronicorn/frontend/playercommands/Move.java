package com.chronicorn.frontend.playercommands;

import com.chronicorn.frontend.Player;

public class Move implements Command {
    private Player player;

    public Move(Player player) {
        this.player = player;
    }

    @Override
    public void execute() {
        // Logic WASD ada di NormalState
        if (!player.isDead()) {
            player.handleInput();
        }
    }
}
