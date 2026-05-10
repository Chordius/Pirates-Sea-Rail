package com.chronicorn.frontend.observers;

import com.badlogic.gdx.graphics.Color;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;

public interface BattlerObserver {
    void onStatsUpdated();
    void onHpChange(int damageTaken, Elements incomingElement);
    default void onEnChange() {};
    void onWeakChange();
    default void onBreakChange() {};
    void onPopupRequested(String text, Color color);
}
