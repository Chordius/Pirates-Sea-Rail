package com.chronicorn.frontend.observers;

public interface BattlerObserver {
    void onStatsUpdated();
    void onHpChange();
    void onEnChange();
    void onWeakChange();
}
