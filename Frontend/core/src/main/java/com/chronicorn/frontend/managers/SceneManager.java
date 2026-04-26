package com.chronicorn.frontend.managers;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ScreenUtils;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.screens.MenuScreen;
import java.util.Stack;

public class SceneManager {
    private static final SceneManager instance = new SceneManager();
    private Stack<Screen> screenStack;
    private Game game;

    private SceneManager() {
        screenStack = new Stack<>();
    }

    public static SceneManager getInstance() {
        return instance;
    }

    public void initialize(Game game) {
        this.game = game;
    }

    public void changeScreen(Screen screen) {
        while (!screenStack.isEmpty()) {
            screenStack.pop().dispose();
        }
        game.setScreen(screen);
    }

    public void pushScreen(Screen screen) {
        if (game.getScreen() != null) {
            screenStack.push(game.getScreen());
        }
        game.setScreen(screen);
    }

    public void transitionToMenu(Player player) {
        Screen currentScreen = game.getScreen();
        TextureRegion snapshot = ScreenUtils.getFrameBufferTexture();
        Image bgImage = new Image(snapshot);
        bgImage.setColor(0.4f, 0.4f, 0.4f, 1f); // Gelapkan sedikit

        if (currentScreen != null) {
            screenStack.push(currentScreen);
        }

        MenuScreen menuScreen = new MenuScreen(bgImage, player);
        game.setScreen(menuScreen);
    }

    public void goBack() {
        if (!screenStack.isEmpty()) {
            Screen previousScreen = screenStack.pop();
            game.setScreen(previousScreen);
        }
    }
}
