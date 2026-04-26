package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.screens.MapScreen;

public class CmdToggleHUD implements EventCommand {
    private boolean isHidden;
    private boolean isFinished = false;

    public CmdToggleHUD(boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public void start() {
        MapScreen screen = LevelMapManager.getInstance().getMapScreen();
        if (screen != null && screen.getGameHUD() != null) {
            screen.getGameHUD().setCutsceneHidden(isHidden);
        }
        isFinished = true;
    }

    @Override
    public void update(float delta) {
        // Tidak butuh update, instan selesai
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
