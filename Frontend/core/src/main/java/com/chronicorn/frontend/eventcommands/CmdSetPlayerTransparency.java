package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdSetPlayerTransparency implements EventCommand {
    private boolean transparent;

    public CmdSetPlayerTransparency(boolean transparent) {
        this.transparent = transparent;
    }

    @Override
    public void start() {
        if (LevelMapManager.getInstance().getPlayer() != null) {
            LevelMapManager.getInstance().getPlayer().setTransparent(transparent);
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
