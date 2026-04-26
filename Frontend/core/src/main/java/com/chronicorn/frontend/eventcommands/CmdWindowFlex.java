package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.windows.WindowFlex;

public class CmdWindowFlex implements EventCommand {

    private WindowFlex window;

    // Config
    private float x, y, width, height;
    private float duration;

    // Content
    private String textToAdd;
    private String imagePathToAdd;

    // Flags
    private boolean isBlocking = true; // Default to blocking the game
    private boolean isTransparent = false;

    // Logic State
    private float timer;
    private boolean isFinished = false;

    public CmdWindowFlex() {
        this.window = new WindowFlex();
        // Defaults
        this.x = 200; this.y = 200; this.width = 400; this.height = 200;
        this.duration = 2.0f;
    }

    // --- BUILDER METHODS ---
    public CmdWindowFlex setText(String text) {
        this.textToAdd = text;
        return this;
    }

    public CmdWindowFlex setImage(String path) {
        this.imagePathToAdd = path;
        return this;
    }

    public CmdWindowFlex setBounds(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        return this;
    }

    public CmdWindowFlex setDuration(float seconds) {
        this.duration = seconds;
        return this;
    }

    public CmdWindowFlex setTransparent(boolean transparent) {
        this.isTransparent = transparent;
        return this;
    }

    public CmdWindowFlex resetContent() {
        window.resetContent();
        return this;
    }

    public CmdWindowFlex addImage(String imagePathToAdd) {
        this.imagePathToAdd = imagePathToAdd;
        return this;
    }

    /**
     * @param blocking If true, player freezes. If false, player moves while window is up.
     */
    public CmdWindowFlex setBlocking(boolean blocking) {
        this.isBlocking = blocking;
        return this;
    }

    @Override
    public void start() {
        // 1. Setup Window Content
        window.resetContent();
        window.move((int)x, (int)y, (int)width, (int)height);
        if (imagePathToAdd != null) window.addImage(imagePathToAdd);
        if (textToAdd != null) window.addTextContent(textToAdd);

        Stage stage = LevelMapManager.getInstance().getMapScreen().stage;
        stage.addActor(window);
        window.open();

        window.setTransparent(this.isTransparent);

        if (isBlocking) {
            timer = 0;
            isFinished = false;
        } else {
            window.addAction(Actions.sequence(
                Actions.delay(duration),
                Actions.fadeOut(0.5f),
                Actions.removeActor() // Removes from stage automatically
            ));
            isFinished = true;
        }
    }

    @Override
    public void update(float delta) {
        // We only run this loop if we are BLOCKING
        if (isBlocking) {
            timer += delta;
            if (timer >= duration) {
                // Manual cleanup
                window.addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.removeActor()
                ));
                isFinished = true;
            }
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
