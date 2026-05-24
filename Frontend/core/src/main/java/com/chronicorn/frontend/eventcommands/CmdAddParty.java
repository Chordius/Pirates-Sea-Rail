package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.battlers.ActorFactory;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;
import com.chronicorn.frontend.managers.networkManager.dto.GachaResult;
import com.chronicorn.frontend.screens.MapScreen;
import com.badlogic.gdx.math.MathUtils;

public class CmdAddParty implements EventCommand {
    private String name;
    private boolean isDone = false;

    public CmdAddParty(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        // Optional: Force start value to ensure smoothness
        // if (fadeIn) screen.fadeAlpha = 1f; else screen.fadeAlpha = 0f
    }

    @Override
    public void update(float delta) {
        if (Main.currentLocalId != null) {
            NetworkManager.grantCharacter(Main.currentLocalId, name, new NetworkCallback<GachaResult>() {
                @Override
                public void onSuccess(GachaResult result) {
                    // 1. Backend has saved it!
                    System.out.println("Granted character: " + result.pulledCharId);

                    // 2. Synchronize the frontend's local session
                    GameSession.getInstance().getParty().unlockCharacter(
                        result.pulledCharId,
                        ActorFactory.createActor(result.pulledCharId)
                    );
                }

                @Override
                public void onError(String errorMessage) {
                    System.err.println("Failed to sync character: " + errorMessage);
                }
            });
        }
        isDone = true;
    }

    @Override
    public boolean isFinished() {
        return isDone;
    }
}
