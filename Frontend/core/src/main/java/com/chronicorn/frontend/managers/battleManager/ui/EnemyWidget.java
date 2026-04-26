package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;

public class EnemyWidget extends Group implements BattlerObserver {
    public interface Listener {
        void onEnemyClicked(Enemy enemy);
        void onEnemyHovered(EnemyWidget widget);
        void onEnemyDied(EnemyWidget widget);
    }

    private Enemy enemy;
    private final Listener listener;

    private Image sprite;
    private float spriteOrigX;
    private float spriteOrigY;

    private ProgressBar hpBar;
    private ProgressBar hpGhostBar;
    private ProgressBar weaknessBar;
    private ProgressBar weaknessGhostBar;
    private Table iconTable;

    private boolean isDead = false;
    private int lastHp = -1;
    private int lastWeakness;

    private Group reticleGroup;

    private final int BAR_SIZE_X = 371;
    private final int BAR_SIZE_Y = 70;
    private static final float MODIFIER = 0.5F;

    public EnemyWidget(final Enemy enemy, Skin skin, final Listener listener) {
        this.enemy = enemy;
        this.listener = listener;
        this.enemy.addObserver(this);

        // 1. Enemy Sprite
        String textureName = "enemy_" + enemy.getName().toLowerCase();
        sprite = new Image(skin.getDrawable(textureName));
        this.setSize(sprite.getWidth(), sprite.getHeight());
        this.setOrigin(Align.center);
        sprite.setTouchable(Touchable.disabled);
        this.addActor(sprite);

        this.spriteOrigX = sprite.getX();
        this.spriteOrigY = sprite.getY();

        // 1.5 Reticle System
        reticleGroup = new Group();
        reticleGroup.setSize(96, 96);
        reticleGroup.setOrigin(Align.center);
        // Center the reticle directly over the middle of the enemy sprite
        reticleGroup.setPosition(this.getWidth() / 2 - reticleGroup.getWidth() / 2, this.getHeight() * 2 / 3 - reticleGroup.getHeight() / 2);
        reticleGroup.setVisible(false);
        reticleGroup.setTouchable(Touchable.disabled);

        Image reticle = new Image(skin.getDrawable("reticle"));
        reticle.setOrigin(Align.center);
        reticle.setPosition(reticleGroup.getWidth() / 2 - reticle.getWidth() / 2, reticleGroup.getHeight() / 2 - reticle.getHeight() / 2);
        reticle.addAction(Actions.forever(Actions.rotateBy(90f, 1f)));

        reticleGroup.addActor(reticle);
        this.addActor(reticleGroup);

        // 2. The Unified Background Frame
        Image barBg = new Image(skin.getDrawable("enemy-bar-bg"));
        barBg.setPosition(this.getWidth() / 2 - barBg.getWidth() / 2, 0);
        barBg.setTouchable(Touchable.disabled);
        this.addActor(barBg);

        // 3. Weakness Bar (Positioned inside the bottom slot of the background)
        weaknessGhostBar = new ProgressBar(0, enemy.getMaxWeakness(), 1, false, skin, "enemy-weakness-ghost-bar");
        weaknessGhostBar.setValue(enemy.getWeaknessBar());
        weaknessGhostBar.setWidth(BAR_SIZE_X * MODIFIER);
        weaknessGhostBar.setHeight(BAR_SIZE_Y * MODIFIER);
        weaknessGhostBar.setPosition(barBg.getX(), barBg.getY());
        weaknessGhostBar.setTouchable(Touchable.disabled);
        weaknessGhostBar.setAnimateDuration(0.6f); // Slow trailing animation
        this.addActor(weaknessGhostBar);

        // 4. Main Weakness Bar
        weaknessBar = new ProgressBar(0, enemy.getMaxWeakness(), 1, false, skin, "enemy-weakness-bar");
        weaknessBar.setValue(enemy.getWeaknessBar());
        weaknessBar.setWidth(BAR_SIZE_X * MODIFIER);
        weaknessBar.setHeight(BAR_SIZE_Y * MODIFIER);
        weaknessBar.setPosition(barBg.getX(), barBg.getY());
        weaknessBar.setTouchable(Touchable.disabled);
        weaknessBar.setAnimateDuration(0f); // Fast snap
        this.addActor(weaknessBar);

        // 4. HP Bar (Positioned inside the top slot of the background)
        hpGhostBar = new ProgressBar(0, enemy.getMaxHp(), 1, false, skin, "enemy-hp-ghost-bar");
        hpGhostBar.setValue(enemy.getHp());
        hpGhostBar.setWidth(BAR_SIZE_X * MODIFIER);
        hpGhostBar.setHeight(BAR_SIZE_Y * MODIFIER);
        hpGhostBar.setPosition(barBg.getX(), barBg.getY());
        hpGhostBar.setTouchable(Touchable.disabled);
        hpGhostBar.setAnimateDuration(1.2f); // Slow trailing animation
        this.addActor(hpGhostBar);

        hpBar = new ProgressBar(0, enemy.getMaxHp(), 1, false, skin, "enemy-hp-bar");
        hpBar.setValue(enemy.getHp());
        hpBar.setWidth(BAR_SIZE_X * MODIFIER);
        hpBar.setHeight(BAR_SIZE_Y * MODIFIER);
        hpBar.setPosition(barBg.getX(), barBg.getY());
        hpBar.setTouchable(Touchable.disabled);
        hpBar.setAnimateDuration(0.1f); // Fast snap
        this.addActor(hpBar);

        // 5. Elemental Icons (Right-aligned, growing left)
        iconTable = new Table();
        Array<Elements> innates = enemy.getInnateElements();

        // Loop backwards so the primary element is placed on the far right
        for (int i = innates.size - 1; i >= 0; i--) {
            Elements el = innates.get(i);
            if (el != Elements.NONE) {
                Image icon = new Image(skin.getDrawable(el.name().toLowerCase()));
                iconTable.add(icon).size(32, 32).pad(2);
            }
        }
        iconTable.pack();

        // Calculate the rightmost edge of the background frame
        float rightEdgeX = barBg.getX() + barBg.getWidth();

        // Subtract the table's width from the right edge.
        // As the table gets wider, it pushes X further to the left.
        iconTable.setPosition(rightEdgeX - iconTable.getWidth(), barBg.getY() + barBg.getHeight());
        iconTable.setTouchable(Touchable.disabled);
        this.addActor(iconTable);

        // 5. Interaction Listeners
        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (EnemyWidget.this.listener != null) {
                    EnemyWidget.this.listener.onEnemyClicked(enemy);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    EnemyWidget.this.clearActions();
                    EnemyWidget.this.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.fade));

                    if (EnemyWidget.this.listener != null) {
                        EnemyWidget.this.listener.onEnemyHovered(EnemyWidget.this);
                    }
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (isDead) return;

                if (pointer == -1) {
                    EnemyWidget.this.clearActions();
                    EnemyWidget.this.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.fade));
                }
            }
        });
    }

    // --- Visual Animation Triggers ---

    private void triggerDamageAnimation() {
        // Reset state to prevent drifting if hit multiple times rapidly
        sprite.clearActions();
        sprite.setColor(Color.WHITE);
        sprite.setPosition(spriteOrigX, spriteOrigY);

        // Position Shake
        sprite.addAction(Actions.sequence(
            Actions.moveBy(15, 0, 0.05f),
            Actions.moveBy(-30, 0, 0.05f),
            Actions.moveBy(15, 0, 0.05f)
        ));

        // Color Flash
        sprite.addAction(Actions.sequence(
            Actions.color(Color.RED, 0.1f),
            Actions.color(Color.WHITE, 0.1f)
        ));

        if (enemy.getHp() <= 0) {
            // Color Flash
            sprite.addAction(Actions.sequence(
                Actions.color(Color.RED, 0.8f)
            ));

            sprite.addAction(Actions.forever(Actions.sequence(
                Actions.moveBy(2, 0, 0.05f),
                Actions.moveBy(-4, 0, 0.05f)
            )));
        }
    }

    // --- State Updates ---

    public void setTargeted(boolean targeted) {
        reticleGroup.setVisible(targeted);
    }

    public void setTargeted(boolean isPrimary, boolean isSecondary) {
        reticleGroup.setVisible(isPrimary || isSecondary);

        // Distinguish visuals
        if (isPrimary) {
            reticleGroup.setScale(1.0f);
        } else if (isSecondary) {
            reticleGroup.setScale(0.6f); // Smaller for adjacent targets
        }
    }

    public Enemy getEnemy() {
        return enemy;
    }

    private void checkDeath() {
        if (!enemy.isAlive() && !isDead) {
            isDead = true;
            if (listener != null) {
                listener.onEnemyDied(this);
            }
        }
    }

    @Override
    public void onStatsUpdated() {

    }

    @Override
    public void onEnChange() {
        // Enemies have no ultimates
    }

    @Override
    public void onHpChange() {
        int currentHp = enemy.getHp();

        // Update both bars. The ghost bar will visually lag due to animateDuration.
        hpBar.setValue(currentHp);
        hpGhostBar.setValue(currentHp);

        if (currentHp < lastHp || lastHp == -1) {
            triggerDamageAnimation();
        }

        lastHp = currentHp;
        checkDeath();
    }

    @Override
    public void onWeakChange() {
        int currentWeakness = enemy.getWeaknessBar();

        weaknessBar.setValue(currentWeakness);
        weaknessGhostBar.setValue(currentWeakness);

        if (currentWeakness < lastWeakness) {
            triggerDamageAnimation();
        }

        lastWeakness = currentWeakness;
    }

    public void dispose() {
        enemy.removeObserver(this);
    }
}
