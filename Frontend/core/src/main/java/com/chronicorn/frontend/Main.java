package com.chronicorn.frontend;

import com.badlogic.gdx.Game;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.screens.BattleScreen;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.screens.TitleScreen;
import com.chronicorn.frontend.screens.MenuScreen;

public class Main extends Game {
    public static String currentUsername = null;

    @Override
    public void create() {

        ImageManager.loadWindowSkin(ImageManager.fontSize);

        SceneManager.getInstance().initialize(this);

        SceneManager.getInstance().pushScreen(new BattleScreen());
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
