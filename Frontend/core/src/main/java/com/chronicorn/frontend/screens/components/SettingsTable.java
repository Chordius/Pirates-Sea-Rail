package com.chronicorn.frontend.screens.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.chronicorn.frontend.Main;
import com.chronicorn.frontend.managers.SceneManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.screens.TitleScreen;

public class SettingsTable extends Table {

    public SettingsTable() {
        super();
        buildUI();
    }

    private void buildUI() {
        this.setFillParent(true);
        this.center();

        // 1. Create the settings container table using paper-ninepatch-bg
        Table container = new Table();
        Drawable paperBg = ImageManager.skin.getDrawable("paper-ninepatch-bg");
        container.setBackground(paperBg);

        // Keep the padding so the rows don't bleed off the physical paper edge
        container.pad(60, 50, 60, 50);

        // 2. Add Title
        Label titleLabel = new Label("SYSTEM SETTINGS", ImageManager.skin, "menu3");
        container.add(titleLabel).padBottom(35).colspan(2).row();

        // 3. Audio Control Rows using label-darkbrown-bg
        // CHANGED: Removed width() and padLeft(). Added expandX() and fillX().
        container.add(createAudioRow("Music Volume", createMusicSlider()))
                .expandX().fillX().height(65).padBottom(15).row();

        container.add(createAudioRow("SFX Volume", createSFXSlider()))
                .expandX().fillX().height(65).padBottom(15).row();

        container.add(createAudioRow("Ambient Volume", createAmbientSlider()))
                .expandX().fillX().height(65).padBottom(35).row();

        // 4. Log Out Button
        TextButton btnLogout = new TextButton("Log Out", ImageManager.skin, "boxed-button");
        btnLogout.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Preferences prefs = Gdx.app.getPreferences("PiratesSeaRailSession");
                prefs.remove("localUserId");
                prefs.flush();

                Main.currentLocalId = null;
                SoundManager.getInstance().playSound("lever.wav");

                SceneManager.getInstance().changeScreen(new TitleScreen());
            }
        });
        container.add(btnLogout).width(220).height(70).row();

        // CHANGED: Increased width to 800, decreased height to 450 to make it wider and
        // shorter.
        this.add(container).width(1000).height(500).padTop(40);
    }

    private Table createAudioRow(String labelText, Actor controlWidget) {
        Table row = new Table();
        // Use Label-DarkBrown.png as the inline row background
        row.setBackground(ImageManager.skin.getDrawable("label-darkbrown-bg"));
        row.padLeft(54).padRight(54);

        Label label = new Label(labelText, ImageManager.skin, "default");
        label.setColor(Color.valueOf("ede8de")); // light paper-colored text for dark brown row

        row.add(label).left().expandX();
        row.add(controlWidget).right().width(240);

        return row;
    }

    private Slider createMusicSlider() {
        final Slider slider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        slider.setValue(SoundManager.getInstance().getMusicVolume());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setMusicVolume(slider.getValue());
            }
        });
        return slider;
    }

    private Slider createSFXSlider() {
        final Slider slider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        slider.setValue(SoundManager.getInstance().getSfxVolume());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setSFXVolume(slider.getValue());
            }
        });
        return slider;
    }

    private Slider createAmbientSlider() {
        final Slider slider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        slider.setValue(SoundManager.getInstance().getAmbientVolume());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().setAmbientVolume(slider.getValue());
            }
        });
        return slider;
    }

    public void playEntranceAnimation() {
        this.clearActions();
        this.getColor().a = 0f;
        this.setScale(0.9f);
        this.setOrigin(Align.center);
        this.addAction(Actions.parallel(
                Actions.fadeIn(0.25f, Interpolation.fade),
                Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)));
    }
}
