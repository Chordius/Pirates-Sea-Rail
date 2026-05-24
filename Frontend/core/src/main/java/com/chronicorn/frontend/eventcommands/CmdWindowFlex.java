package com.chronicorn.frontend.eventcommands;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.windows.GabWindow;

public class CmdWindowFlex implements EventCommand {

    private String textToAdd;
    private boolean isFinished = false;

    public CmdWindowFlex() {
    }

    // --- BUILDER METHODS (kept for compatibility) ---
    public CmdWindowFlex setText(String text) {
        this.textToAdd = text;
        return this;
    }

    public CmdWindowFlex setImage(String path) {
        return this;
    }

    public CmdWindowFlex setBounds(float x, float y, float w, float h) {
        return this;
    }

    public CmdWindowFlex setDuration(float seconds) {
        return this;
    }

    public CmdWindowFlex setTransparent(boolean transparent) {
        return this;
    }

    public CmdWindowFlex resetContent() {
        return this;
    }

    public CmdWindowFlex addImage(String imagePathToAdd) {
        return this;
    }

    public CmdWindowFlex setBlocking(boolean blocking) {
        return this;
    }

    @Override
    public void start() {
        GabWindow window = new GabWindow();
        if (textToAdd != null) {
            window.addTextContent(textToAdd);
        }

        Stage stage = LevelMapManager.getInstance().getMapScreen().stage;
        stage.addActor(window);
        window.open(stage);

        // They are fire-and-forget and should not pause the game
        isFinished = true;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
