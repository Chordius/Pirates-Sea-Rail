package com.chronicorn.frontend.scripts;
import com.chronicorn.frontend.managers.eventManagers.EventManager;

public interface MapScript {
    /** Called immediately when the map loads (Auto-run events) */
    void onMapLoad(EventManager events);

    /** Called when player touches a rectangle in "Logic" layer */
    void onTrigger(String triggerName, EventManager events);
}



