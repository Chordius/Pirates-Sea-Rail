package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.PhysicsObjects;

public class CmdShowBalloon implements EventCommand {
    private String entityName;
    private String balloonType;
    private boolean isWait;

    private PhysicsObjects targetEntity = null;
    private float timer = 0f;
    private final float totalDuration = 1.2f; // RPG Maker standard duration (8 frames * 0.1s)

    public CmdShowBalloon(String entityName, String balloonType, boolean isWait) {
        this.entityName = entityName;
        this.balloonType = balloonType;
        this.isWait = isWait;
    }

    @Override
    public void start() {
        if (entityName.equalsIgnoreCase("player")) {
            targetEntity = LevelMapManager.getInstance().getPlayer();
        } else {
            targetEntity = LevelMapManager.getInstance().getObjectByName(entityName);
        }

        if (targetEntity != null) {
            LevelMapManager.getInstance().showBalloon(targetEntity, balloonType);
            timer = 0f;
        }
    }

    @Override
    public void update(float delta) {
        if (targetEntity != null && isWait) {
            timer += delta;
        }
    }

    @Override
    public boolean isFinished() {
        if (targetEntity == null) return true;
        if (isWait) {
            return timer >= totalDuration;
        }
        return true;
    }
}
