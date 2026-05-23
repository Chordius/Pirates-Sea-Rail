package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.managers.assetManager.ImageManager;

public class GachaAnimationScreen implements Screen {
    private Stage stage;
    private Runnable onAnimationComplete;

    public GachaAnimationScreen(Runnable onAnimationComplete) {
        this.onAnimationComplete = onAnimationComplete;
        this.stage = new Stage(new ScreenViewport());
        buildCutscene();
    }

    private void buildCutscene() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        Group cameraGroup = new Group();
        cameraGroup.setSize(sw, sh);
        cameraGroup.setOrigin(Align.center);

        Image background = new Image(ImageManager.skin.getDrawable("gacha-bg-character"));
        background.setFillParent(true);
        background.setScaling(com.badlogic.gdx.utils.Scaling.fill);
        cameraGroup.addActor(background);

        // --- TEXT POSITIONS & INITIAL ROTATIONS ---
        Image textCome = new Image(ImageManager.skin.getDrawable("gacha-text-come"));
        textCome.setPosition(sw * 0.65f, sh * 0.6f);
        textCome.setOrigin(Align.center);
        textCome.setRotation(-1f);
        textCome.getColor().a = 0f;

        Image textBring = new Image(ImageManager.skin.getDrawable("gacha-text-bring"));
        textBring.setPosition(sw * 0.02f, sh * 0.37f);
        textBring.setOrigin(Align.center);
        textBring.setRotation(1f);
        textBring.getColor().a = 0f;

        Image textHopes = new Image(ImageManager.skin.getDrawable("gacha-text-hopes"));
        textHopes.setPosition(sw * 0.64f, sh * 0.08f);
        textHopes.setOrigin(Align.center);
        textHopes.setRotation(-1f);
        textHopes.getColor().a = 0f;

        cameraGroup.addActor(textCome);
        cameraGroup.addActor(textBring);
        cameraGroup.addActor(textHopes);

        // The White Flash Overlay (Start fully opaque)
        final Image whiteFlash = new Image(ImageManager.skin.getDrawable("white-pixel"));
        whiteFlash.setColor(1f, 1f, 1f, 1f);
        whiteFlash.setFillParent(true);

        stage.addActor(cameraGroup);
        stage.addActor(whiteFlash);

        // ==========================================
        // ACTION SEQUENCE: THE DIRECTOR'S SCRIPT
        // ==========================================

        // SHOT 0 (INITIAL STATE): Deeply zoomed in on the sun, aggressively rotated
        // We move the group DOWN (-sh * 0.7f) to pull the top-center sun into the middle of the screen
        cameraGroup.setScale(4.5f);
        cameraGroup.setPosition(0f, -sh * 0.7f);
        cameraGroup.setRotation(-145f);

        float panSpeed = 0.6f;
        float splatSpeed = 0.25f;

        // Exaggerated fast-slow easing for the camera whip pans
        Interpolation cameraEase = Interpolation.exp10Out;

        stage.addAction(Actions.sequence(
            Actions.delay(0.2f), // Brief split-second hold in pure white

            // 1. THE SUN SPIN: Fade from white, zoom out, and untwist (Kept as requested)
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    whiteFlash.addAction(Actions.fadeOut(1.2f, Interpolation.pow2Out));
                    cameraGroup.addAction(Actions.parallel(
                        Actions.moveTo(sw * 0.05f, -sh * 0.7f, 1.5f, cameraEase),
                        Actions.scaleTo(2.5f, 2.5f, 2f, cameraEase),
                        Actions.rotateTo(3f, 1.5f, cameraEase)
                    ));
                }
            }),
            Actions.delay(1.5f), // Wait for the spin to finish
            Actions.delay(0.3f), // HOLD ON THE SUN FOR 2 SECONDS

            // 2. SHOT 1: Whip pan to "Come On Board" (Focus: Top-Right)
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    whiteFlash.addAction(Actions.sequence(
                        Actions.alpha(0.7f),
                        Actions.fadeOut(panSpeed, Interpolation.pow2Out)
                    ));
                    // Mathematically bounded translation to prevent edge exposure
                    cameraGroup.addAction(Actions.parallel(
                        Actions.moveTo(-sw * 0.4f, -sh * 0.33f, panSpeed, cameraEase),
                        Actions.scaleTo(2f, 2f, panSpeed, cameraEase),
                        Actions.rotateTo(4f, panSpeed, cameraEase)
                    ));
                }
            }),
            Actions.delay(panSpeed * 0.7f), // Splat the text just before the camera fully settles
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    textCome.setScale(3.0f);
                    textCome.addAction(Actions.parallel(
                        Actions.fadeIn(splatSpeed),
                        Actions.scaleTo(1f, 1f, splatSpeed, Interpolation.swingOut)
                    ));
                }
            }),
            Actions.delay(0.5f),

            // 3. SHOT 2: Whip pan to "Bring Along" (Focus: Middle-Left)
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    whiteFlash.addAction(Actions.sequence(
                        Actions.alpha(0.6f),
                        Actions.fadeOut(panSpeed, Interpolation.pow2Out)
                    ));
                    // Mathematically bounded translation to prevent edge exposure
                    cameraGroup.addAction(Actions.parallel(
                        Actions.moveTo(sw * 0.36f, sh * 0.1f, panSpeed, cameraEase),
                        Actions.scaleTo(1.9f, 1.9f, panSpeed, cameraEase),
                        Actions.rotateTo(-3f, panSpeed, cameraEase)
                    ));
                }
            }),
            Actions.delay(panSpeed * 0.7f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    textBring.setScale(3.0f);
                    textBring.addAction(Actions.parallel(
                        Actions.fadeIn(splatSpeed),
                        Actions.scaleTo(1f, 1f, splatSpeed, Interpolation.swingOut)
                    ));
                }
            }),
            Actions.delay(0.7f),

            // 4. SHOT 3: Whip pan to "All Your Hopes" (Focus: Bottom-Right)
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    whiteFlash.addAction(Actions.sequence(
                        Actions.alpha(0.6f),
                        Actions.fadeOut(panSpeed, Interpolation.pow2Out)
                    ));
                    // Mathematically bounded translation to prevent edge exposure
                    cameraGroup.addAction(Actions.parallel(
                        Actions.moveTo(-sw * 0.23f, sh * 0.22f, panSpeed, cameraEase),
                        Actions.scaleTo(1.5f, 1.5f, panSpeed, cameraEase),
                        Actions.rotateTo(2f, panSpeed, cameraEase)
                    ));
                }
            }),
            Actions.delay(panSpeed * 0.7f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    textHopes.setScale(3.0f);
                    textHopes.addAction(Actions.parallel(
                        Actions.fadeIn(splatSpeed),
                        Actions.scaleTo(1f, 1f, splatSpeed, Interpolation.swingOut)
                    ));
                }
            }),
            Actions.delay(0.8f),

            // 5. FINAL ZOOM OUT: Reveal the full scene
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    // Zoom the camera back to normal
                    cameraGroup.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.9f, cameraEase),
                        Actions.moveTo(0f, 0f, 0.9f, cameraEase),
                        Actions.rotateTo(0f, 0.9f, cameraEase)
                    ));

                    // Create the growing flare from the sun
                    final Image sunFlare = new Image(ImageManager.skin.getDrawable("gacha-sun-flare"));
                    sunFlare.setSize(10f, 10f); // Start as a tiny point
                    // Position at the sun's location (approx top-center of the screen)
                    sunFlare.setPosition(sw * 0.5f - 20f, sh * 0.78f - 5f);
                    sunFlare.setOrigin(Align.center);
                    sunFlare.setScale(0);
                    sunFlare.getColor().a = 0.5f; // Full brightness initially
                    stage.addActor(sunFlare);

                    // Animate the flare exploding outward and fading
                    sunFlare.addAction(Actions.sequence(
                        Actions.parallel(
                            // Scale massively to consume the whole screen and beyond
                            Actions.scaleTo(sw * 0.5f, sw * 0.5f, 1.2f, Interpolation.pow3In),
                            // Wait a fraction of a second, then fade out smoothly
                            Actions.sequence(
                                Actions.delay(1f),
                                Actions.fadeOut(0.3f, Interpolation.swingOut)
                            )
                        ),
                        Actions.removeActor() // Clean up to prevent memory leaks
                    ));
                }
            }),
            Actions.delay(0.7f),

            // 6. THE FLASH OUT TO RESULTS
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    whiteFlash.addAction(Actions.sequence(
                        Actions.fadeIn(0.15f), // Violent flash bang
                        Actions.delay(0.2f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                if (onAnimationComplete != null) {
                                    onAnimationComplete.run();
                                }
                            }
                        })
                    ));
                }
            })
        ));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
