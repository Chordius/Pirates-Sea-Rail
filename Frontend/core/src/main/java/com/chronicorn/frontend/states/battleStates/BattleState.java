package com.chronicorn.frontend.states.battleStates;

import com.chronicorn.frontend.Player;

public interface BattleState {
    void beforeEval();
    void afterEval();
    void respondEval();
    void engageEval();
    void update(Player player, float delta);
}
