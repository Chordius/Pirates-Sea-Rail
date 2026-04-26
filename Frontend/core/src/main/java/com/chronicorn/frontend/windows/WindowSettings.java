package com.chronicorn.frontend.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.Player;

public class WindowSettings extends WindowBase {

    private WindowOptions parentWindow;
    private Runnable onBackAction;
    private Preferences prefs;

    private int focusedIndex = 0;
    private Table[] rows;
    private Actor[] widgets;

    // --- Untuk IN-GAME (Dipanggil dari Pause Menu) ---
    public WindowSettings(WindowOptions parentWindow, Player player) {
        super("",
            (Gdx.graphics.getWidth() / 2) - 225,
            (Gdx.graphics.getHeight() / 2) - 225,
            450, 450
        );
        this.parentWindow = parentWindow;
    }

    // --- Untuk TITLE SCREEN  ---
    public WindowSettings(Runnable onBackAction) {
        super("",
            (Gdx.graphics.getWidth() / 2) - 225,
            (Gdx.graphics.getHeight() / 2) - 225,
            450, 450);
        this.onBackAction = onBackAction;
        this.parentWindow = null;
    }

    @Override
    public void createContents() {
        this.setDebug(false);
        this.prefs = Gdx.app.getPreferences("GameSettings");
        this.clearChildren();

        // Setup Widgets
        final CheckBox fullScreenCheck = new CheckBox(" Fullscreen Mode", ImageManager.skin);
        fullScreenCheck.setChecked(Gdx.graphics.isFullscreen());

        final Slider musicSlider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        musicSlider.setValue(SoundManager.getInstance().getMusicVolume());

        final Slider sfxSlider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        sfxSlider.setValue(SoundManager.getInstance().getSfxVolume());

        final Slider ambientSlider = new Slider(0f, 1f, 0.1f, false, ImageManager.skin, "default-horizontal");
        ambientSlider.setValue(SoundManager.getInstance().getAmbientVolume());

        TextButton btnBack = new TextButton("Back", ImageManager.skin);

        widgets = new Actor[]{fullScreenCheck, musicSlider, sfxSlider, ambientSlider, btnBack};
        rows = new Table[widgets.length];

        rows[0] = createRow(fullScreenCheck, "", 0);
        rows[1] = createRow(musicSlider, "Music Volume", 1);
        rows[2] = createRow(sfxSlider, "SFX Volume", 2);
        rows[3] = createRow(ambientSlider, "Ambient Volume", 3);
        rows[4] = createRow(btnBack, "", 4);

        for (Table row : rows) {
            this.add(row).expandX().fillX().height(60).pad(2).row();
        }

        setupListeners(fullScreenCheck, musicSlider, sfxSlider, ambientSlider, btnBack);
        setupKeyboardInput();

        updateVisualFocus();
    }

    private Table createRow(final Actor widget, String labelText, final int index) {
        Table row = new Table();
        row.left().pad(0, 20, 0, 20);

        if (!labelText.isEmpty()) {
            Label lbl = new Label(labelText, ImageManager.skin);
            row.add(lbl).width(180).left();
        } else {
            row.add().width(0);
        }

        if (widget instanceof TextButton || widget instanceof CheckBox) {
            row.add(widget).left().expandX();
        } else {
            row.add(widget).width(200).right().expandX();
        }

        row.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    focusedIndex = index;
                    updateVisualFocus();
                }
            }
        });
        return row;
    }

    private void changeFocus(int newIndex) {
        if (newIndex < 0) newIndex = rows.length - 1;
        if (newIndex >= rows.length) newIndex = 0;
        focusedIndex = newIndex;
        updateVisualFocus();
    }

    private void updateVisualFocus() {
        Drawable selectionBox = ImageManager.skin.getDrawable("selection-box");
        for (int i = 0; i < rows.length; i++) {
            if (i == focusedIndex) {
                rows[i].setBackground(selectionBox);
                rows[i].addAction(Actions.forever(Actions.sequence(Actions.alpha(0.6f, 0.4f), Actions.alpha(1f, 0.4f))));
            } else {
                rows[i].setBackground((Drawable) null);
                rows[i].clearActions();
                rows[i].getColor().a = 1f;
            }
        }
    }

    private void setupKeyboardInput() {
        this.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    changeFocus(focusedIndex - 1);
                    return true;
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    changeFocus(focusedIndex + 1);
                    return true;
                }

                Actor current = widgets[focusedIndex];
                if (current instanceof Slider) {
                    Slider s = (Slider) current;
                    if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                        s.setValue(s.getValue() - 0.1f);
                        return true;
                    }
                    if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                        s.setValue(s.getValue() + 0.1f);
                        return true;
                    }
                }

                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    if (current instanceof CheckBox) {
                        ((CheckBox) current).setChecked(!((CheckBox) current).isChecked());
                    } else {
                        InputEvent click = new InputEvent();
                        click.setType(InputEvent.Type.touchDown);
                        current.fire(click);
                        click.setType(InputEvent.Type.touchUp);
                        current.fire(click);
                    }
                    return true;
                }

                if (keycode == Input.Keys.ESCAPE) {
                    goBack();
                    return true;
                }

                return false;
            }
        });
    }

    private void setupListeners(CheckBox fs, Slider m, Slider s, Slider a, TextButton b) {
        fs.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (fs.isChecked()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else Gdx.graphics.setWindowedMode(1280, 720);
                prefs.putBoolean("fullscreen", fs.isChecked());
                prefs.flush();
            }
        });
        m.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { SoundManager.getInstance().setMusicVolume(m.getValue()); } });
        s.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { SoundManager.getInstance().setSFXVolume(s.getValue()); } });
        a.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { SoundManager.getInstance().setAmbientVolume(a.getValue()); } });

        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goBack();
            }
        });
    }

    private void goBack() {
        SoundManager.getInstance().playSound("lever.wav");
        Stage stageRef = getStage();
        Runnable callback = this.onBackAction;
        WindowOptions parent = this.parentWindow;
        this.remove();

        if (parent != null) {
            parent.setVisible(true);
            if (stageRef != null) stageRef.setKeyboardFocus(parent);
        } else if (callback != null) {
            callback.run();
        }
    }

}
