package com.chronicorn.frontend.custom;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class HorizontalCropDrawable extends TextureRegionDrawable {

    private Color tintColor = null;

    public HorizontalCropDrawable(TextureRegion region) {
        super(region);
    }

    // Override the native tint method to prevent conversion to SpriteDrawable
    @Override
    public Drawable tint(Color tint) {
        HorizontalCropDrawable drawable = new HorizontalCropDrawable(getRegion());
        drawable.tintColor = tint;
        drawable.setMinWidth(getMinWidth());
        drawable.setMinHeight(getMinHeight());
        return drawable;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        if (width <= 0 || getMinWidth() == 0) return;

        float percent = width / getMinWidth();
        TextureRegion region = getRegion();
        float u = region.getU();
        float v = region.getV();
        float u2 = u + (region.getU2() - u) * percent;
        float v2 = region.getV2();

        // Save original batch color, apply our tint, draw, and revert
        Color tempColor = null;
        if (tintColor != null) {
            tempColor = batch.getColor();
            // batch.setColor applies a tint to the texture drawn below
            batch.setColor(tintColor.r, tintColor.g, tintColor.b, tintColor.a * tempColor.a);
        }

        // Remember we swapped v2 and v to prevent the upside-down bug
        batch.draw(region.getTexture(), x, y, width, height, u, v2, u2, v);

        if (tintColor != null) {
            batch.setColor(tempColor);
        }
    }
}
