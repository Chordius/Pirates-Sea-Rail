package com.chronicorn.frontend;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.chronicorn.frontend.battlers.parties.Deal;
import com.chronicorn.frontend.battlers.parties.Porter;
import com.chronicorn.frontend.battlers.parties.Reyna;
import com.chronicorn.frontend.battlers.parties.Sailor;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.animationManager.AnimationManager;
import com.chronicorn.frontend.managers.ShaderManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.screens.BattleScreen;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.screens.TitleScreen;
import com.chronicorn.frontend.skills.SkillDatabase;
import com.chronicorn.frontend.statuseffect.StatusEffectDatabase;
import com.chronicorn.frontend.battlers.EnemyDatabase;
import com.chronicorn.frontend.managers.networkManager.NetworkManager;
import com.chronicorn.frontend.managers.networkManager.NetworkCallback;
import com.chronicorn.frontend.managers.networkManager.dto.UserAuthResponse;

public class Main extends Game {
    public static String currentLocalId = null;

    private float connectionCheckTimer = 30.0f;
    private static final float CONNECTION_CHECK_INTERVAL = 30.0f;
    private int failedConnectionChecks = 0;
    private static final int MAX_FAILED_CHECKS = 3;

    @Override
    public void create() {
        SkillDatabase.init();

        StatusEffectDatabase.init();

        EnemyDatabase.init();

        ImageManager.loadWindowSkin(ImageManager.fontSize);

        SceneManager.getInstance().initialize(this);

        SceneManager.getInstance().pushScreen(new TitleScreen());
    }

    @Override
    public void render() {
        super.render();

        if (currentLocalId != null && getScreen() != null && !(getScreen() instanceof TitleScreen)) {
            connectionCheckTimer -= Gdx.graphics.getDeltaTime();
            if (connectionCheckTimer <= 0) {
                connectionCheckTimer = CONNECTION_CHECK_INTERVAL;
                checkBackendConnection();
            }
        }
    }

    private void checkBackendConnection() {
        NetworkManager.getUserInfo(currentLocalId, new NetworkCallback<UserAuthResponse>() {
            @Override
            public void onSuccess(UserAuthResponse result) {
                // Connection is maintained, reset failure count
                failedConnectionChecks = 0;
            }

            @Override
            public void onError(String errorMessage) {
                Gdx.app.postRunnable(() -> {
                    failedConnectionChecks++;
                    System.err.println("Backend connection check failed (" + failedConnectionChecks + "/" + MAX_FAILED_CHECKS + "): " + errorMessage);
                    if (failedConnectionChecks >= MAX_FAILED_CHECKS) {
                        if (getScreen() != null && !(getScreen() instanceof TitleScreen)) {
                            System.err.println("Backend connection permanently lost. Booting to TitleScreen.");
                            currentLocalId = null; // Clear login session
                            failedConnectionChecks = 0;
                            SceneManager.getInstance().changeScreen(new TitleScreen());
                        }
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {
        // Dispose global managers and static caches
        try { SoundManager.getInstance().dispose(); } catch (Exception ignored) {}
        try { AnimationManager.dispose(); } catch (Exception ignored) {}
        try { ShaderManager.dispose(); } catch (Exception ignored) {}
        ImageManager.dispose();
        super.dispose();
    }
}
