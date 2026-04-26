package com.chronicorn.frontend.projectiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;

public class BossProjectile {
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private float rotation;
    public boolean isActive = true;
    private float lifeTime = 0;

    // --- Ganti TextureRegion jadi Animation ---
    private static Animation<TextureRegion> projectileAnimation;
    private float stateTime = 0; // Untuk timer animasi

    public BossProjectile(float x, float y, float angleDeg) {
        this.position = new Vector2(x, y);
        this.rotation = angleDeg;

        float speed = 350f;
        this.velocity = new Vector2(MathUtils.cosDeg(angleDeg) * speed, MathUtils.sinDeg(angleDeg) * speed);

        // --- LOAD GAMBAR BULAN SABIT ---
        if (projectileAnimation == null) {
            try {
                Texture tex = new Texture(Gdx.files.internal("Projectiles.png"));
                TextureRegion[][] tmp = TextureRegion.split(tex, tex.getWidth() / 3, tex.getHeight() / 4);
                TextureRegion[] frames = new TextureRegion[3 * 4];
                int index = 0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 3; j++) {
                        frames[index++] = tmp[i][j];
                    }
                }
                //Animasi Proyektil
                projectileAnimation = new Animation<>(0.05f, frames);
                projectileAnimation.setPlayMode(Animation.PlayMode.LOOP);

            } catch (Exception e) {
                System.out.println("Error loading projectile texture: " + e.getMessage());
            }
        }
        this.bounds = new Rectangle(x, y, 48, 48);
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x - bounds.width/2, position.y - bounds.height/2);

        stateTime += delta;
        lifeTime += delta;
        if(lifeTime > 5f) isActive = false;
    }

    public void checkCollision(Player player) {
        if(isActive && bounds.overlaps(player.getBounds())) {
            //Berikan Damage ke Player
            player.takeDamage(5);
            isActive = false;

            System.out.println("PLAYER KENA TEMBAK!");
        }
    }

    public void draw(SpriteBatch batch) {
        if (projectileAnimation != null) {
            TextureRegion currentFrame = projectileAnimation.getKeyFrame(stateTime, true);

            // Warna biru cyan (opsional, biarkan jika sudah suka)
            batch.setColor(0f, 0.7f, 1f, 1f);

            float ukuranBaru = 48f; // Ukuran visual yang baru
            float setengah = ukuranBaru / 2f;

            // --- UPDATE DRAW ---
            batch.draw(currentFrame,
                position.x - setengah, position.y - setengah, // Posisi (di-offset agar center)
                setengah, setengah,                            // Origin (titik putar di tengah)
                ukuranBaru, ukuranBaru,                        // Lebar dan Tinggi baru
                1f, 1f,                                        // Scale
                rotation);                                     // Rotasi

            // --- RESET WARNA ---
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }
}
