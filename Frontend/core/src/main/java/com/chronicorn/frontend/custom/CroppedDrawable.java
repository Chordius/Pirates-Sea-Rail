package com.chronicorn.frontend.custom;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CroppedDrawable extends TextureRegionDrawable {

    public CroppedDrawable(TextureRegion region) {
        super(region);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        TextureRegion reg = getRegion();

        // Calculate what percentage of the bar is currently filled
        // getMinHeight() holds the 100% scaled height you set in your ImageManager
        float percent = height / getMinHeight();

        // Safety check to prevent dividing by zero or drawing negative sizes
        if (percent <= 0f) return;
        if (percent > 1f) percent = 1f;

        // Calculate the exact cropped dimensions in the source texture
        int srcWidth = reg.getRegionWidth();
        int srcHeight = (int) (reg.getRegionHeight() * percent);

        // Calculate the Y offset to crop from the top.
        // In LibGDX, texture V coordinates go top-to-bottom, so adding to Y moves the crop downwards.
        int srcX = reg.getRegionX();
        int srcY = reg.getRegionY() + (reg.getRegionHeight() - srcHeight);

        // Draw only the strictly cropped portion of the texture
        batch.draw(reg.getTexture(),
            x, y,
            width, height, // The on-screen size calculated by ProgressBar
            srcX, srcY,
            srcWidth, srcHeight, // The cropped source texture coordinates
            false, false);
    }
}
