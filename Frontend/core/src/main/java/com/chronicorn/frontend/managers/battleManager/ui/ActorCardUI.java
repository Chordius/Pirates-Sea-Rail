package com.chronicorn.frontend.managers.battleManager.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.ShaderManager;
import com.chronicorn.frontend.managers.battleManager.enums.Elements;
import com.chronicorn.frontend.observers.BattlerObserver;

public class ActorCardUI extends Group implements BattlerObserver {
    private Battler battler;
    private ProgressBar hpBar;
    private ProgressBar hpCatchupBar;
    private Label hpLabel;
    private ProgressBar ultBar;
    private Group ultGroup;
    private Image ultGhost;
    private Image portrait;
    private Image activeIndicator;


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

    public ActorCardUI(Battler battler, Skin skin) {
        this.battler = battler;

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

        // LAYER 1 (Top): Numbers
        String hpNumbers = String.valueOf(battler.getHp());
        hpLabel = new Label((CharSequence) hpNumbers, skin);
        hpLabel.setPosition(130, 15); // Placed over the right side of the HP bar
        this.addActor(hpLabel);
    }

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

    public Battler getBattler() {
        return battler;
    }

    @Override
    public void onStatsUpdated() {

    }

    @Override
    public void onHpChange() {
        float currentHp = hpBar.getValue();
        float newHp = battler.getHp();

        if (newHp < currentHp) {
            playHitAnimation();
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

    public void dispose() {
        battler.removeObserver(this);
    }
}
