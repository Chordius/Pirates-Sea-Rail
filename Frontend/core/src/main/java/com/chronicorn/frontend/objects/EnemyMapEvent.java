package com.chronicorn.frontend.objects;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.eventManagers.EventManager;

public class EnemyMapEvent extends InteractiveObject {

    private String enemyGroupId;

    public EnemyMapEvent(String name, float x, float y, float width, float height, String enemyGroupId) {
        super(name, x, y, width, height);
        this.enemyGroupId = enemyGroupId;
        this.isSolid = true; // They block the player
    }

    // Normal encounter (They touched you, or you walked into them)
    @Override
    public void interact(Player player, EventManager events) {
        if (events.isBusy()) return;

        System.out.println("Normal Battle Start: " + enemyGroupId);
        // TODO: Fire your event command to transition to the Battle Screen normally
    }

    // Advantage encounter (You struck them with Z)
    public void strikeAdvantage(Player player, EventManager events) {
        if (events.isBusy()) return;

        System.out.println("Player Advantage Battle Start! " + enemyGroupId);
        // TODO: Fire your event command to transition to the Battle Screen with a preemptive strike flag
    }
}
