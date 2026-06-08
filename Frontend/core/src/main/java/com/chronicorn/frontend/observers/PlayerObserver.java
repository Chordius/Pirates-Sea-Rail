package com.chronicorn.frontend.observers;

public interface PlayerObserver {
    default void onHealthChanged(int currentHp, int maxHp) {}
    default void onDashCooldownChanged(float currentTimer, float maxTime) {}
    default void onPlayerStatusChanged(boolean isDead) {}
}
