package com.chronicorn.frontend.monsters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.projectiles.SkeletonArrow;

import java.util.ArrayList;
import java.util.List;

public class Skeleton extends Monster {

    private float attackTimer = 0f;
    public List<SkeletonArrow> arrows = new ArrayList<>();
    private Player targetPlayer;

    public Skeleton(Animation<TextureRegion> anim, float x, float y) {
        super(anim, x, y, 50, 32, 20, false, 70f, 0.3f, 400f);

        this.walkSoundFile = "skeleton_walk1.wav";
    }

    @Override
    public void update(float delta, Player player) {
        this.targetPlayer = player;
        super.update(delta, player); // Parent handle walk sound automatically

        for (int i = 0; i < arrows.size(); i++) {
            SkeletonArrow arrow = arrows.get(i);
            arrow.update(delta);
            arrow.checkCollision(player);

            if (!arrow.isActive) {
                arrows.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void updateAI(float delta) {
        if (targetPlayer == null) return;
        Vector2 playerPos = targetPlayer.getPosition();

        float tooCloseDistance = 200f;
        float stopDistance = 400f;
        float distanceToPlayer = this.position.dst(playerPos);
        Vector2 direction = new Vector2(playerPos).sub(this.position).nor();

        if (distanceToPlayer < tooCloseDistance) {
            this.velocity.set(direction).scl(-speed * 0.8f);
        } else if (distanceToPlayer > stopDistance) {
            this.velocity.set(direction).scl(speed);
        } else {
            this.velocity.set(0, 0);
        }

        attackTimer += delta;
        if (distanceToPlayer <= 450f && attackTimer > 2.0f) {
            shoot(playerPos);
            attackTimer = 0;
        }
    }

    private void shoot(Vector2 targetPos) {
        arrows.add(new SkeletonArrow(this.position.x, this.position.y, targetPos.x, targetPos.y));

        // PLAY SFX SHOOT
        SoundManager.getInstance().playSound("skeleton_shoot1.wav");
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        Color oldColor = batch.getColor();
        batch.setColor(Color.WHITE);
        for (SkeletonArrow arrow : arrows) {
            arrow.draw(batch);
        }
        batch.setColor(oldColor);
    }
}
