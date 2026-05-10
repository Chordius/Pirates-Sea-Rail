package com.chronicorn.frontend;

import com.badlogic.gdx.Game;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.screens.BattleScreen;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.screens.TitleScreen;

public class Main extends Game {
    public static String currentLocalId = null;

    @Override
    public void create() {

        ImageManager.loadWindowSkin(ImageManager.fontSize);

        SceneManager.getInstance().initialize(this);

        SceneManager.getInstance().pushScreen(new MapScreen());
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        ImageManager.dispose();
    }
}
