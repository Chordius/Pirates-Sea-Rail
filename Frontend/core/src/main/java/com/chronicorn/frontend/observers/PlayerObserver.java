package com.chronicorn.frontend.observers;

public interface PlayerObserver {
    void onHealthChanged(int currentHp, int maxHp);
    void onDashCooldownChanged(float currentTimer, float maxTime);
    void onPlayerStatusChanged(boolean isDead);
}
