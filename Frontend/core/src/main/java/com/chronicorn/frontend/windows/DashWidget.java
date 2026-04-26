package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class DashWidget extends Actor {
    private TextureRegion icon;
    private float cooldownPercent = 0f;

    public DashWidget() {
        Texture tex = new Texture(Gdx.files.internal("Dash_Icon.png"));
        this.icon = new TextureRegion(tex);
        setSize(64, 64);
    }

    public void updateCooldown(float current, float max) {
        if (max > 0) {
            this.cooldownPercent = current / max;
        } else {
            this.cooldownPercent = 0;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();

        // 1. Gambar Ikon Asli (Normal / Terang)
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(icon, getX(), getY(), getWidth(), getHeight());

        // 2. Gambar Efek Cooldown (Overlay)
        if (cooldownPercent > 0) {
            batch.setColor(0f, 0f, 0f, 0.6f * parentAlpha);

            TextureRegion overlay = new TextureRegion(icon);

            float visibleHeight = getHeight() * cooldownPercent;
            float oldV = overlay.getV();
            float oldV2 = overlay.getV2();

            float newV = oldV + (oldV2 - oldV) * (1 - cooldownPercent);
            overlay.setV(newV);

            batch.draw(overlay, getX(), getY(), getWidth(), visibleHeight);
        }

        batch.setColor(Color.WHITE);
    }
}
