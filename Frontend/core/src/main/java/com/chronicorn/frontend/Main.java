package com.chronicorn.frontend;

import com.badlogic.gdx.Game;
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

public class Main extends Game {
    public static String currentLocalId = null;

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
