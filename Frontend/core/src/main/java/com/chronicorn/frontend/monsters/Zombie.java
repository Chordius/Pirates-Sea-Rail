package com.chronicorn.frontend.monsters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager;

public class Zombie extends Monster {

    private static TextureRegion staticEffect;
    private float attackTimer = 0f;
    private float attackCooldown = 2.0f;

    private boolean isAttacking = false;
    private float preparationTimer = 0f;
    private final float ATTACK_DELAY = 0.6f;

    private boolean showEffect = false;
    private float effectDisplayTimer = 0f;
    private final float EFFECT_DURATION = 0.3f;

    private Player targetPlayer;

    public Zombie(Animation<TextureRegion> anim, float x, float y) {
        super(anim, x, y, 100, 10, 5, false, 50f, 0.3f, 500f);

        if (staticEffect == null) {
            try {
                Texture sheet = new Texture(Gdx.files.internal("Projectiles.png"));
                staticEffect = new TextureRegion(sheet, 0, 0, 64, 64);
            } catch (Exception e) {
                Gdx.app.log("ZOMBIE_LOAD", "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(float delta, Player player) {
        this.targetPlayer = player;
        super.update(delta, player);

        if (showEffect) {
            effectDisplayTimer += delta;
            if (effectDisplayTimer >= EFFECT_DURATION) {
                showEffect = false;
            }
        }
    }

    @Override
    protected void updateAI(float delta) {
        if (targetPlayer == null) return;

        float distance = this.position.dst(targetPlayer.getPosition());
        float range = 60f;

        if (isAttacking) {
            velocity.set(0, 0);

            preparationTimer += delta;
            if (preparationTimer >= ATTACK_DELAY) {
                if (distance < 75f) {
                    targetPlayer.takeDamage(this.atk);
                    showEffect = true;
                    effectDisplayTimer = 0;
                }
                isAttacking = false;
                preparationTimer = 0;
                attackTimer = 0;
            }
        } else {
            attackTimer += delta;

            if (distance < range && attackTimer >= attackCooldown) {
                isAttacking = true;
                velocity.set(0, 0);

                SoundManager.getInstance().playSound("zombie_grunt1.wav");

            } else if (distance >= range) {
                Vector2 dir = new Vector2(targetPlayer.getPosition()).sub(this.position).nor();
                velocity.set(dir.x * speed, dir.y * speed);
            } else {
                velocity.set(0, 0);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        if (showEffect && staticEffect != null) {
            com.badlogic.gdx.graphics.Color oldColor = batch.getColor().cpy();
            batch.setColor(com.badlogic.gdx.graphics.Color.RED);

            float centerX = position.x + (bounds.width / 2) - 32;
            float centerY = position.y + (bounds.height / 2) - 32;

            batch.draw(staticEffect, centerX, centerY, 64, 64);
            batch.setColor(oldColor);
        }
    }
}
