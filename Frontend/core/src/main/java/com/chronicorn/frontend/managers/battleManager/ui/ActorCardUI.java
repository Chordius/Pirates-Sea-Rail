package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.ShaderManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;
import com.chronicorn.frontend.statuseffect.StatusEffect;

public class ActorCardUI extends Group implements BattlerObserver {
    public interface Listener {
        void onAllyClicked(Battler clickedAlly);
        void onAllyHovered(ActorCardUI card);
    }
    private Listener listener;
    private boolean isPrimaryTarget = false;
    private boolean isSecondaryTarget = false;

    private Battler battler;
    private ProgressBar hpBar;
    private ProgressBar hpCatchupBar;
    private Label hpLabel;
    private ProgressBar ultBar;
    private Group ultGroup;
    private Image ultGhost;
    private Image portrait;
    private Image activeIndicator;
    private Group reticleGroup;

    private Skin skin;
    private Table statusTable;
    private float statusRotationTimer = 0f;
    private int statusOffset = 0;
    private static final float STATUS_ROTATION_INTERVAL = 2.0f;

    private final int SIZE_X = 330;
    private final int SIZE_Y = 240;
    private final float MODIFIER = 0.5F;

    private final int PORTRAIT_SIZE = 450;
    private final int PORTRAIT_OFFSET_Y = 18;

    private final int ULTIMATE_OFFSET_X = 130;
    private final int ULTIMATE_OFFSET_Y = 70;
    private final int ULTIMATE_SIZE = 173;

    private final int HP_SIZE_X = 340;
    private final int HP_SIZE_Y = 43;

    private final float NORMAL_SCALE = 1.0f;
    private final float ACTIVE_SCALE = 1.2f;

    public ActorCardUI(Battler battler, Skin skin, Listener listener) {
        this.battler = battler;
        this.listener = listener;
        this.skin = skin;
        this.battler.addObserver(this);

        // Define the bounds of this whole card (adjust to your assets)
        this.setSize(SIZE_X * MODIFIER, SIZE_Y * MODIFIER);

        // LAYER 6: Active Turn Indicator (Added BEFORE portrait to render behind it)
        activeIndicator = new Image(skin.getDrawable("user_indicator"));
        // Match the portrait size and coordinates
        activeIndicator.setSize(180, 180);
        activeIndicator.setPosition(-5, -25 + PORTRAIT_OFFSET_Y * MODIFIER);
        activeIndicator.setOrigin(Align.center);
        activeIndicator.setVisible(false);
        // Continuous rotation
        activeIndicator.addAction(Actions.forever(Actions.rotateBy(-90f, 1f)));
        this.addActor(activeIndicator);

        // LAYER 5 (Bottom): Brown PNG background base
        Image baseBg = new Image(skin.getDrawable("brown_base"));
        baseBg.setFillParent(true); // Fills the 160x140 area
        this.addActor(baseBg);

        // LAYER 4: Ultimate Bar Background & Foreground (Circular)
        ultBar = new ProgressBar(0, battler.getMaxEnergy(), 1, true, skin, "ult-battle-bar" + battler.getName().toLowerCase()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                ShaderProgram oldShader = batch.getShader();

                if (ShaderManager.additiveGlowShader != null && ShaderManager.additiveGlowShader.isCompiled()) {
                    batch.setShader(ShaderManager.additiveGlowShader);
                }

                super.draw(batch, parentAlpha);

                batch.setShader(oldShader);
            }
        };
        ultBar.setColor(Color.BLACK);
        ultBar.setValue(battler.getEnergy());
        ultBar.setSize(ULTIMATE_SIZE, ULTIMATE_SIZE);

        ultGroup = new Group();
        ultGroup.setTransform(true);
        ultGroup.setSize(ULTIMATE_SIZE, ULTIMATE_SIZE);
        // We apply your 0.5f modifier as the BASE scale
        ultGroup.setScale(MODIFIER);
        ultGroup.setPosition(ULTIMATE_OFFSET_X * MODIFIER, ULTIMATE_OFFSET_Y * MODIFIER); // Top Right of the card
        ultGroup.addActor(ultBar);
        this.addActor(ultGroup);

        ultGhost = new Image(skin.getDrawable("ult-battle-bar" + battler.getName().toLowerCase() + "_ghost"));
        ultGhost.setSize(ULTIMATE_SIZE, ULTIMATE_SIZE);
        ultGhost.setOrigin(Align.center);
        ultGhost.setTouchable(Touchable.disabled);
        ultGhost.setColor(Color.CLEAR); // Start invisible
        ultGroup.addActor(ultGhost);

        // LAYER 3: Player Portrait
        portrait = new Image(skin.getDrawable("portrait_" + battler.getName().toLowerCase())) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                // Save the default shader
                ShaderProgram oldShader = batch.getShader();

                // Apply the custom shader if it compiled successfully
                if (ShaderManager.portraitFadeShader != null && ShaderManager.portraitFadeShader.isCompiled()) {
                    batch.setShader(ShaderManager.portraitFadeShader);
                }

                // Draw the portrait with the shader active
                super.draw(batch, parentAlpha);

                // Restore the default shader immediately
                batch.setShader(oldShader);
            }
        };
        portrait.setOrigin(100, 0);
        portrait.setPosition(-50, -50 + PORTRAIT_OFFSET_Y * MODIFIER);
        portrait.setSize(200, 200);
        this.addActor(portrait);

        // Layer 2.5: HP Catch-Up Bar
        hpCatchupBar = new ProgressBar(0, battler.getMaxHp(), 1, false, skin, "hp-catchup-bar");
        hpCatchupBar.setValue(battler.getHp());
        hpCatchupBar.setPosition(3, -5);
        hpCatchupBar.setSize(HP_SIZE_X * 0.5f, HP_SIZE_Y * 0.5f);

        // This tells the bar to take 0.5 seconds to visually tween to the new value
        hpCatchupBar.setAnimateDuration(2f);
        this.addActor(hpCatchupBar);

        // LAYER 2: HP Foreground/Background
        hpBar = new ProgressBar(0, battler.getMaxHp(), 1, false, skin, "hp-battle-bar");
        hpBar.setValue(battler.getHp());
        hpBar.setPosition(3, -5);
        hpBar.setSize(HP_SIZE_X * 0.5f, HP_SIZE_Y * 0.5f);
        hpBar.setAnimateDuration(0f);
        this.addActor(hpBar);

        statusTable = new Table();
        statusTable.left().bottom();
        // Position X matches HP bar (3), Position Y sits just above the HP bar
        statusTable.setPosition(3, 20);
        statusTable.setTouchable(Touchable.disabled);
        this.addActor(statusTable);

        // LAYER 1 (Top): Numbers
        String hpNumbers = String.valueOf(battler.getHp());
        hpLabel = new Label((CharSequence) hpNumbers, skin, "number-style");
        hpLabel.setPosition(124, 18); // Placed over the right side of the HP bar
        this.addActor(hpLabel);

        // LAYER 0: Target
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

        // Touchable Listener
        this.setTouchable(Touchable.disabled);
        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) listener.onAllyClicked(getBattler());
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (listener != null) listener.onAllyHovered(ActorCardUI.this);
            }
        });
    }

    // Set Card UIs to be something.
    public void setActive(boolean isActive) {
        float scale = isActive ? ACTIVE_SCALE : NORMAL_SCALE;

        // Scale the components
        activeIndicator.addAction(Actions.scaleTo(scale, scale, 0.2f, Interpolation.pow2Out));
        portrait.addAction(Actions.scaleTo(scale, scale, 0.2f, Interpolation.pow2Out));
        ultGroup.addAction(Actions.scaleTo(scale * MODIFIER, scale * MODIFIER, 0.1f));

        activeIndicator.setVisible(isActive);

        // Optional: Bring the active card to the front of the stage
        // so the enlarged portrait doesn't go behind other cards
        if (isActive) this.toFront();
    }

    public void setTargeted(boolean primary, boolean secondary) {
        this.isPrimaryTarget = primary;
        this.isSecondaryTarget = secondary;
        reticleGroup.setVisible(primary || secondary);

        // TODO: Add your visual changes here (e.g., make the card glow, move it up slightly, or show a reticle)
        if (isPrimaryTarget) {
            reticleGroup.setScale(1.0f);
        } else if (isSecondaryTarget) {
            reticleGroup.setScale(0.6f); // Smaller for adjacent targets
        }
    }

    public Battler getBattler() {
        return battler;
    }

    @Override
    public void onStatsUpdated() {
        refreshStatusIcons();
    }

    @Override
    public void onHpChange(int baseDamage, Elements element) {
        float currentHp = hpBar.getValue();
        float newHp = battler.getHp();

        if (newHp < currentHp) {
            int damageTaken = (int) (currentHp - newHp);
            playHitAnimation();
            spawnPopup(String.valueOf(damageTaken), Color.WHITE);
        }

        // 2. Update the progress bars
        // The green bar snaps instantly to the lower value
        hpBar.setValue(newHp);

        // The red bar updates its target value, but animates downward over 0.5 seconds
        hpCatchupBar.setValue(newHp);

        // Update text
        hpLabel.setText(String.valueOf(battler.getHp()));
    }

    @Override
    public void onEnChange() {
        float currentEnergy = ultBar.getValue();
        float newEnergy = battler.getEnergy();

        if (newEnergy != currentEnergy) {
            ultBar.setValue(newEnergy);
            ultBar.clearActions();

            if (!(battler instanceof Actor)) return;
            Color themeColor = getCharacterThemeColor(((Actor) battler).getElement());

            // Check if we JUST reached or crossed the Max Energy threshold
            if (newEnergy >= battler.getMaxEnergy()) {

                // Only trigger the "Expansion Ghost" if we weren't already at max
                if (currentEnergy < battler.getMaxEnergy()) {
                    triggerMaxEnergyFlare(themeColor);
                }

                // A: Initial "Entry" Flash (0.3s) then B: Continuous Loop (0.6s)
                ultBar.addAction(Actions.sequence(
                    // Initial highlight upon reaching max
                    Actions.color(themeColor, 0.3f, Interpolation.pow2Out),
                    Actions.color(Color.BLACK, 0.3f, Interpolation.pow2In),
                    // Perpetual loop
                    Actions.forever(
                        Actions.sequence(
                            Actions.color(themeColor, 0.6f, Interpolation.sine),
                            Actions.color(Color.BLACK, 0.6f, Interpolation.sine)
                        )
                    )
                ));

            } else if (newEnergy > currentEnergy) {
                // Standard gain flash (0.15s)
                ultBar.addAction(Actions.sequence(
                    Actions.color(themeColor, 0.15f),
                    Actions.color(Color.BLACK, 0.15f)
                ));
            } else {
                // Energy consumed
                ultBar.setColor(Color.BLACK);
                ultGhost.clearActions();
                ultGhost.setColor(Color.CLEAR);
            }
        }
    }

    @Override
    public void onWeakChange() {

    }

    private void triggerMaxEnergyFlare(Color themeColor) {
        ultGhost.clearActions();

        // Reset ghost state
        ultGhost.setScale(1.0f);
        ultGhost.setColor(themeColor);
        ultGhost.getColor().a = 0.8f; // Start semi-transparent

        // Pulse expansion: Scale up while fading out
        ultGhost.addAction(Actions.parallel(
            Actions.scaleTo(2f, 2f, 0.5f, Interpolation.exp5Out),
            Actions.fadeOut(0.5f, Interpolation.exp5Out)
        ));
    }

    // Animation Things
    private void playHitAnimation() {
        // Clear any previous actions in case the battler is hit rapidly
        portrait.clearActions();

        // Ensure we know exactly where it belongs to prevent drift
        float origX = -50;
        float origY = -50 + PORTRAIT_OFFSET_Y * MODIFIER;

        // 1. The Wiggle Action
        portrait.addAction(Actions.sequence(
            Actions.moveBy(15, 0, 0.05f),
            Actions.moveBy(-30, 0, 0.05f),
            Actions.moveBy(15, 0, 0.05f),
            Actions.moveTo(origX, origY) // Force it back to exact starting coordinates
        ));

        // 2. The Color Tint Action
        // The portrait Image natively multiplies its color into the SpriteBatch,
        // which your fragment shader accepts via 'v_color * texColor'.
        portrait.addAction(Actions.sequence(
            Actions.color(Color.SCARLET, 0.1f),
            Actions.color(Color.WHITE, 0.1f)
        ));
    }

    // --- Status Effect Logic ---

    public void refreshStatusIcons() {
        statusTable.clearChildren();

        // Assuming your Battler class holds the active states array
        Array<StatusEffect> states = battler.getActiveStates();

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

            // Generate the icon
            Image icon = new Image(skin.getDrawable(state.getIconId()));
            // Sized slightly smaller than enemies (24x24 instead of 32x32) to fit the card UI better
            statusTable.add(icon).size(24, 24).padRight(4);
            count++;
        }
        statusTable.pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Array<StatusEffect> states = battler.getActiveStates();

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

    private Color getCharacterThemeColor(Elements name) {
        switch (name) {
            case WIND:
                return Color.valueOf("82cd98");
            case FIRE:
                return Color.valueOf("dc5151");
            case WATER:
                return Color.valueOf("82abcd");
            case LIGHTNING:
                return Color.valueOf("aa61b7");
            case EARTH:
                return Color.valueOf("8B572A");
            // Add other characters and their corresponding LibGDX Color constants
            default:
                return Color.BLACK;
        }
    }

    @Override
    public void onPopupRequested(String text, Color color) {
        spawnPopup(text, color);
    }

    private void spawnPopup(String text, Color color) {
        if (this.getParent() == null) return; // Failsafe if widget isn't on stage yet

        // Calculate spawn position (Center X, slightly above Center Y)
        float spawnX = this.getX() + (this.getWidth() / 2) - 20; // -20 offset to center the text roughly
        float spawnY = this.getY() + (this.getHeight() * 0.75f);

        // Optional: Add slight random X offset so numbers don't perfectly overlap if hit rapidly
        spawnX += (float) (Math.random() * 40 - 20);

        FloatingPopup popup = new FloatingPopup(text, skin, "damage-style", color, spawnX, spawnY);

        // Add to the parent stage so it renders freely over top of everything
        this.getParent().addActor(popup);
    }

    public void dispose() {
        battler.removeObserver(this);
    }
}
