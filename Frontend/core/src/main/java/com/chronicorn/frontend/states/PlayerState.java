package com.chronicorn.frontend.states;

import com.chronicorn.frontend.Player;

public interface PlayerState {
    void handleInput(Player player);
    void update(Player player, float delta);
    void onDashCommand(Player player);
}
