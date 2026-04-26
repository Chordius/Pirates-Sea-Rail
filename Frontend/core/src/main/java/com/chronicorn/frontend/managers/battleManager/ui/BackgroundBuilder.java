package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class BackgroundBuilder {
    private String drawableName;
    private float width = -1;
    private float height = -1;
    private Color tint = Color.WHITE;

    public BackgroundBuilder setTexture(String drawableName) {
        this.drawableName = drawableName;
        return this;
    }

    public BackgroundBuilder setSize(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public BackgroundBuilder setTint(Color tint) {
        this.tint = tint;
        return this;
    }

    public Image build(Skin skin) {
        if (drawableName == null || drawableName.isEmpty()) {
            throw new IllegalStateException("Background texture must be set.");
        }

        Image bg = new Image(skin.getDrawable(drawableName));

        // If size wasn't explicitly set, default to the texture's native size
        if (width != -1 && height != -1) {
            bg.setSize(width, height);
        }

        bg.setColor(tint);
        bg.setPosition(0, 0);

        return bg;
    }
}
