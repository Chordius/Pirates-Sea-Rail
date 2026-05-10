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
import com.chronicorn.frontend.statuseffect.StatusEffect;

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
    private Skin skin;

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

    private Table statusTable;
    private float statusRotationTimer = 0f;
    private int statusOffset = 0;
    private static final float STATUS_ROTATION_INTERVAL = 2.0f;

    private Group markGroup;
    private Image markIcon;
    private Image markGhost;

    public EnemyWidget(final Enemy enemy, Skin skin, final Listener listener) {
        this.enemy = enemy;
        this.listener = listener;
        this.skin = skin;
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

        // Elemental Mark UI (Positioned over the far-right icon)
        markGroup = new Group();
        markGroup.setSize(32, 32);

        // Calculate position: Right edge of the table minus the icon size and padding
        float markX = iconTable.getX() + iconTable.getWidth() - 34f;
        float markY = iconTable.getY() + 2f; // +2 to account for the table's pad(2)
        markGroup.setPosition(markX, markY);
        markGroup.setTouchable(Touchable.disabled);
        markGroup.setVisible(false); // Hidden by default

        markIcon = new Image();
        markIcon.setSize(32, 32);
        markGroup.addActor(markIcon);

        markGhost = new Image();
        markGhost.setSize(32, 32);
        markGhost.setColor(new Color(1f, 1f, 1f, 0f)); // Start transparent
        markGroup.addActor(markGhost);

        this.addActor(markGroup);

        statusTable = new Table();
        statusTable.left().bottom();
        statusTable.setPosition(barBg.getX(), barBg.getY() + barBg.getHeight());
        statusTable.setTouchable(Touchable.disabled);
        this.addActor(statusTable);

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

    public void playCastAnimation() {
        this.setOrigin(Align.center);

        // 1. Perspective Math Setup
        // Distance from the object to the Vanishing Point.
        // -400f means the object is 400 pixels below the horizon line.
        float distanceToVpY = -220f;
        float distanceToVpX = 0f;    // Centered horizontally

        float mainTargetScale = 1.1f;
        float ghostTargetScale = 1.5f;

        // Calculate total required translation for the main widget
        float mainMoveY = distanceToVpY * (mainTargetScale - 1f);
        float mainMoveX = distanceToVpX * (mainTargetScale - 1f);

        // 2. Scale and move the main enemy container
        this.addAction(Actions.parallel(
            Actions.scaleTo(mainTargetScale, mainTargetScale, 0.3f, Interpolation.pow2Out),
            Actions.moveBy(mainMoveX, mainMoveY, 0.3f, Interpolation.pow2Out),
            Actions.delay(0.3f)
        ));

        // 3. Create the Ghost Layer
        Image ghost = new Image(sprite.getDrawable());
        ghost.setSize(sprite.getWidth(), sprite.getHeight());
        ghost.setPosition(sprite.getX(), sprite.getY());
        ghost.setOrigin(Align.center);
        this.addActor(ghost);

        // 4. Ghost Choreography
        ghost.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.parallel(
                Actions.color(Color.GOLD),
                Actions.alpha(0.4f),
                Actions.scaleTo(ghostTargetScale, ghostTargetScale, 0.3f, Interpolation.pow2Out),
                Actions.moveBy(0, 0, 0.3f, Interpolation.pow2Out),
                Actions.fadeOut(0.3f, Interpolation.pow2Out)
            ),
            Actions.removeActor()
        ));
    }

    public void resetCastAnimation() {
        float distanceToVpY = -220f;
        float distanceToVpX = 0f;
        float mainTargetScale = 1.1f;

        // Invert the exact movement used in the cast animation
        float reverseMoveY = -(distanceToVpY * (mainTargetScale - 1f));
        float reverseMoveX = -(distanceToVpX * (mainTargetScale - 1f));

        this.addAction(Actions.parallel(
            Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.pow2Out),
            Actions.moveBy(reverseMoveX, reverseMoveY, 0.2f, Interpolation.pow2Out)
        ));
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

    // --- Status Effect Logic ---

    public void refreshStatusIcons() {
        System.out.println("Status!");
        statusTable.clearChildren();

        // Assuming your Enemy/Battler class has this getter
        Array<StatusEffect> states = enemy.getActiveStates();

        if (states == null || states.size == 0) return;

        // Failsafe: Reset rotation if states expired and the array shrank
        if (states.size <= 3 || statusOffset >= states.size) {
            statusOffset = 0;
            statusRotationTimer = 0f;
        }

        // Draw up to 3 icons starting from the current offset
        int count = 0;
        for (int i = statusOffset; i < states.size && count < 3; i++) {
            StatusEffect state = states.get(i);

            // Generate the icon. Assumes StatusEffect has a getIconId() method
            Image icon = new Image(skin.getDrawable(state.getIconId()));
            statusTable.add(icon).size(32, 32).pad(2);
            count++;
        }

        statusTable.pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Array<StatusEffect> states = enemy.getActiveStates();

        // Only run the rotation timer if there are more than 3 active states
        if (states != null && states.size > 3) {
            statusRotationTimer += delta;

            if (statusRotationTimer >= STATUS_ROTATION_INTERVAL) {
                statusRotationTimer = 0f;
                statusOffset += 3; // Jump forward by 3

                // Wrap around back to the beginning
                if (statusOffset >= states.size) {
                    statusOffset = 0;
                }
                refreshStatusIcons();
            }
        }
    }

    @Override
    public void onStatsUpdated() {
        refreshStatusIcons();
        refreshSignatureLossUI();
    }

    @Override
    public void onHpChange(int baseDamage, Elements element) {
        if (lastHp == -1) {
            lastHp = enemy.getMaxHp();
        }
        int currentHp = enemy.getHp();

        // Update both bars. The ghost bar will visually lag due to animateDuration.
        hpBar.setValue(currentHp);
        hpGhostBar.setValue(currentHp);

        if (lastHp != -1 && currentHp < lastHp) {
            int damageTaken = lastHp - currentHp;
            triggerDamageAnimation();

            // Spawn standard damage number (White)
            spawnDamageNumber(damageTaken, element);
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

        refreshSignatureLossUI();

        lastWeakness = currentWeakness;
    }

    public void refreshSignatureLossUI() {
        boolean isBroken = enemy.isInSignatureLoss();

        // 1. Darken or Restore Innate Icons
        for (com.badlogic.gdx.scenes.scene2d.Actor child : iconTable.getChildren()) {
            if (isBroken) {
                // Darken to a heavy gray/black
                child.setColor(0.2f, 0.2f, 0.2f, 1f);
            } else {
                // Restore normal color
                child.setColor(Color.WHITE);
            }
        }

        // 2. Handle the Mark Overlay
        if (isBroken) {
            // Assuming your Enemy class has a getter for the current ElementMark object
            if (enemy.getCurrentElementalMark() != null) {
                Elements markElement = enemy.getCurrentElementalMark().element;
                String markTextureName = markElement.name().toLowerCase();

                // Set the correct elemental graphic
                markIcon.setDrawable(skin.getDrawable(markTextureName));
                markGhost.setDrawable(skin.getDrawable(markTextureName));

                markGroup.setVisible(true);

                // Apply the glow pulse effect
                markGhost.clearActions();
                markGhost.addAction(Actions.forever(
                    Actions.sequence(
                        Actions.alpha(0.8f, 0.6f, Interpolation.sine),
                        Actions.alpha(0.2f, 0.6f, Interpolation.sine)
                    )
                ));
            } else {
                // Broken, but waiting for a new mark to be applied
                markGroup.setVisible(false);
            }
        } else {
            // Not broken, hide the mark overlay completely
            markGroup.setVisible(false);
            markGhost.clearActions();
        }
    }

    @Override
    public void onPopupRequested(String text, Color color) {
        spawnReactionPopup(text, color);
    }

    private void spawnDamageNumber(int damage, Elements element) {
        if (this.getParent() == null) return;

        float x = getX() + getWidth() / 2f;
        float y = getY() + getHeight() / 3f;

        Color finalColor = element.getElementColor();

        // The popup will now spawn white, then transition to finalColor
        FloatingPopup popup = new FloatingPopup(String.valueOf(damage), skin, "damage-style", finalColor, x, y);
        this.getParent().addActor(popup);
    }

    private void spawnReactionPopup(String text, Color tint) {
        if (this.getParent() == null) return;

        // 1. Create the popup at 0,0 first so we can measure its exact width
        FloatingPopup popup = new FloatingPopup(text, skin, "reaction-style", tint, 0, 0);

        // 2. Calculate the boundaries
        float minX = getX();
        float maxX = getX() + getWidth() - popup.getWidth(); // Right edge MINUS text width

        // Failsafe: If the text is somehow wider than the entire enemy sprite, lock it to the left edge
        if (maxX < minX) {
            maxX = minX;
        }

        // 3. Roll a random X within those strict bounds
        float spawnX = minX + (float) (Math.random() * (maxX - minX));
        float spawnY = getY() + getHeight() / 2f;

        popup.setPosition(spawnX, spawnY);
        this.getParent().addActor(popup);
    }

    public void dispose() {
        enemy.removeObserver(this);
    }
}
