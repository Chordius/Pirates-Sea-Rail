package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdSetSteppingAnimation implements EventCommand {
    private String entityName;
    private boolean stepping;

    /**
     * Creates a new command to enable or disable stationary stepping animation.
     * @param entityName The name of the map event entity to target.
     * @param stepping True to turn on stepping animation, false to turn it off.
     */
    public CmdSetSteppingAnimation(String entityName, boolean stepping) {
        this.entityName = entityName;
        this.stepping = stepping;
    }

    @Override
    public void start() {
        InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);
        if (obj instanceof MapEvent) {
            ((MapEvent) obj).setSteppingAnimation(stepping);
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
