package com.chronicorn.frontend.projectiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class SkeletonArrow {
    private Vector2 position;
    private Vector2 velocity;
    public Rectangle bounds;
    private float rotation;
    public boolean isActive = true;
    private float lifeTime = 1.5f;

    private static TextureRegion texture;

    public SkeletonArrow(float x, float y, float targetX, float targetY) {
        this.position = new Vector2(x, y);

        float angleRad = MathUtils.atan2(targetY - y, targetX - x);
        this.rotation = angleRad * MathUtils.radiansToDegrees + 90;

        float speed = 300f;
        this.velocity = new Vector2(MathUtils.cos(angleRad) * speed, MathUtils.sin(angleRad) * speed);

        this.bounds = new Rectangle(x, y, 10, 24);

        if (texture == null) {
            try {
                Texture arrowImg = new Texture(Gdx.files.internal("Arrow.png"));
                texture = new TextureRegion(arrowImg);

            } catch (Exception e) {
                System.out.println("CRASH: Gagal load arrow_solo.png.");
            }
        }
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        // 2. Kurangi durasi hidup (agar tidak terbang selamanya)
        lifeTime -= delta;
        if (lifeTime <= 0) {
            isActive = false;
        }
    }

    public void checkCollision(Player player) {
        if(isActive && bounds.overlaps(player.getBounds())) {
            player.takeDamage(10);
            isActive = false;
        }
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture,
                position.x, position.y,
                24, 24,
                48, 48,
                1f, 1f,
                rotation);
        }
    }
}
