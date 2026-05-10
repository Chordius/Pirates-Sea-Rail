package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.windows.WindowLogin; // <--- Import WindowLogin
import com.chronicorn.frontend.windows.WindowSettings;

public class TitleScreen implements Screen {
    private Stage stage;
    private Table mainTable;
    private Texture titleLogoTexture;

    // Variabel Navigasi Keyboard
    private int focusedIndex = 0;
    private TextButton[] buttons;

    public TitleScreen() {
        stage = new Stage(new ScreenViewport());

        try {
            titleLogoTexture = new Texture(Gdx.files.internal("title.png"));
            titleLogoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("TitleScreen", "Logo image not found: title.png");
        }

        SoundManager.getInstance().playMusic("song_intro.mp3");
        setupUI();

        // Setup Input Listener untuk Keyboard
        setupKeyboardNavigation();
    }

    private void setupUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // --- TITLE LOGO ---
        if (titleLogoTexture != null) {
            Image titleImage = new Image(titleLogoTexture);
            titleImage.setScaling(Scaling.fit);
            mainTable.add(titleImage).width(700).height(200).padBottom(50).row();
        }

        // --- BUTTONS ---
        TextButton playButton = createTitleButton("START GAME", 0);
        TextButton settingsButton = createTitleButton("SETTINGS", 1);
        TextButton exitButton = createTitleButton("EXIT", 2);

        buttons = new TextButton[]{playButton, settingsButton, exitButton};

        // --- LOGIC KLIK (DIUBAH UNTUK LOGIN) ---
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound("gate.wav");

                // [PERBAIKAN UTAMA]
                // Jangan langsung masuk MapScreen. Buka Login Window dulu.
                openLoginWindow();
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound("lever.wav");
                openSettings();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Tambahkan tombol ke Table
        mainTable.add(playButton).width(350).height(70).padBottom(20).row();
        mainTable.add(settingsButton).width(350).height(70).padBottom(20).row();
        mainTable.add(exitButton).width(350).height(70).row();

        updateVisualFocus();
    }

    // --- METHOD BARU: BUKA LOGIN ---
    private void openLoginWindow() {
        // Sembunyikan menu utama agar tidak tumpang tindih
        mainTable.setVisible(false);

        // Buat Window Login
        WindowLogin loginWindow = new WindowLogin();
        loginWindow.open();

        // Letakkan di tengah layar
        loginWindow.setPosition(
            (Gdx.graphics.getWidth() - loginWindow.getWidth()) / 2,
            (Gdx.graphics.getHeight() - loginWindow.getHeight()) / 2
        );

        // Tambahkan ke stage
        stage.addActor(loginWindow);

        // (Opsional) Jika ingin tombol Back di login window berfungsi:
        // Anda perlu modifikasi WindowLogin untuk menerima Runnable callback seperti Settings,
        // Tapi untuk sekarang restart game saja jika ingin batal login.
    }

    // --- METHOD: BUKA SETTINGS (Sudah ada tapi dirapikan) ---
    private void openSettings() {
        mainTable.setVisible(false);

        // Gunakan logika callback agar bisa kembali ke menu utama
        WindowSettings settingsWindow = new WindowSettings(new Runnable() {
            @Override
            public void run() {
                // Saat tombol Back ditekan di Settings:
                mainTable.setVisible(true);
                Gdx.input.setInputProcessor(stage);
                stage.setKeyboardFocus(null);
                updateVisualFocus();
            }
        });

        stage.addActor(settingsWindow);
        stage.setKeyboardFocus(settingsWindow);
    }

    // Helper untuk membuat tombol
    private TextButton createTitleButton(String text, final int index) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(ImageManager.skin.get(TextButton.TextButtonStyle.class));
        TextButton button = new TextButton(text, style);
        button.getLabel().setFontScale(1.5f);

        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    focusedIndex = index;
                    updateVisualFocus();
                }
            }
        });
        return button;
    }

    private void setupKeyboardNavigation() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (mainTable.isVisible()) {
                    if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                        changeFocus(focusedIndex - 1);
                        return true;
                    }
                    if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                        changeFocus(focusedIndex + 1);
                        return true;
                    }
                    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                        InputEvent click = new InputEvent();
                        click.setType(InputEvent.Type.touchDown);
                        buttons[focusedIndex].fire(click);
                        click.setType(InputEvent.Type.touchUp);
                        buttons[focusedIndex].fire(click);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void changeFocus(int newIndex) {
        if (newIndex < 0) newIndex = buttons.length - 1;
        if (newIndex >= buttons.length) newIndex = 0;
        focusedIndex = newIndex;
        updateVisualFocus();
    }

    private void updateVisualFocus() {
        Drawable selectionBox = ImageManager.skin.getDrawable("selection-box");
        for (int i = 0; i < buttons.length; i++) {
            if (i == focusedIndex) {
                buttons[i].getStyle().up = selectionBox;
                buttons[i].clearActions();
                buttons[i].addAction(Actions.forever(
                    Actions.sequence(Actions.alpha(0.5f, 0.4f), Actions.alpha(1.0f, 0.4f))
                ));
            } else {
                buttons[i].getStyle().up = null;
                buttons[i].clearActions();
                buttons[i].getColor().a = 1f;
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (titleLogoTexture != null) titleLogoTexture.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
