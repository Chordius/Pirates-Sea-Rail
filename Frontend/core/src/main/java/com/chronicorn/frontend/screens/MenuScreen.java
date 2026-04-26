package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.ImageManager;
import com.chronicorn.frontend.windows.WindowLogin;
import com.chronicorn.frontend.windows.WindowOptions;
import com.chronicorn.frontend.windows.WindowSettings;

public class MenuScreen implements Screen {
    private Stage stage;
    private Image backgroundImage;

    // Simpan Table utama agar bisa di-hide/show
    private Table rootTable;

    public MenuScreen(Image bgImage, Player player) {
        this.stage = new Stage(new ScreenViewport());

        // 1. SETUP BACKGROUND
        if (bgImage != null) {
            this.backgroundImage = bgImage;
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage);
        }

        // 2. LOGIKA PEMISAH (HYBRID)
        if (player != null) {
            // --- MODE PAUSE (Di dalam Game) ---
            WindowOptions optionsWindow = new WindowOptions(player);
            optionsWindow.open();
            optionsWindow.setPosition(
                (Gdx.graphics.getWidth() - optionsWindow.getWidth()) / 2,
                (Gdx.graphics.getHeight() - optionsWindow.getHeight()) / 2
            );
            stage.addActor(optionsWindow);

        } else {
            // --- MODE TITLE SCREEN (Awal Game) ---
            setupTitleScreen();
        }

        Gdx.input.setInputProcessor(stage);
    }

    private void setupTitleScreen() {
        // UI Layout Utama
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // --- 1. JUDUL BESAR "CHRONICORN" ---
        Label titleLabel = new Label("Chronicorn", ImageManager.skin);
        titleLabel.setFontScale(4.0f); // Perbesar Font agar mirip screenshot
        titleLabel.setAlignment(Align.center);

        // Tambahkan judul (Padding bawah besar biar jarak ke tombol jauh)
        rootTable.add(titleLabel).padBottom(80).row();

        // --- 2. TOMBOL-TOMBOL MENU ---
        Table btnTable = new Table();

        // START GAME: Pakai style "boxed-button" (Ada Kotaknya)
        TextButton btnStart = new TextButton("START GAME", ImageManager.skin, "boxed-button");

        // SETTINGS & EXIT: Pakai style "default" (Hanya Teks)
        TextButton btnSettings = new TextButton("SETTINGS", ImageManager.skin, "default");
        TextButton btnExit = new TextButton("EXIT", ImageManager.skin, "default");

        // Susun Tombol
        btnTable.add(btnStart).width(250).height(60).padBottom(20).row(); // Tombol Start Besar
        btnTable.add(btnSettings).padBottom(15).row();
        btnTable.add(btnExit).row();

        rootTable.add(btnTable);

        // --- 3. LOGIKA KLIK TOMBOL (ANTI-NUMPLEK) ---

        // Aksi START GAME -> Buka Login
        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Sembunyikan Menu Utama
                rootTable.setVisible(false);

                // Tampilkan Login
                WindowLogin loginWindow = new WindowLogin();
                loginWindow.open();
                loginWindow.setPosition(
                    (Gdx.graphics.getWidth() - loginWindow.getWidth()) / 2,
                    (Gdx.graphics.getHeight() - loginWindow.getHeight()) / 2
                );

                // (Opsional) Tambahkan logika tombol Back di WindowLogin nanti jika perlu
                stage.addActor(loginWindow);
            }
        });

        // Aksi SETTINGS -> Buka Settings (Hanya UI Dummy dulu karena blm ada player)
        btnSettings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                rootTable.setVisible(false);

                // Gunakan WindowSettings tapi pass null sebagai player/parent sementara
                // Atau buat instance khusus jika perlu.
                // Di sini kita pakai cara trick agar WindowOptions bisa handle
                WindowOptions dummyOptions = new WindowOptions(null);
                WindowSettings settingsWindow = new WindowSettings(dummyOptions, null);
                settingsWindow.open();
                settingsWindow.setPosition(
                    (Gdx.graphics.getWidth() - settingsWindow.getWidth()) / 2,
                    (Gdx.graphics.getHeight() - settingsWindow.getHeight()) / 2
                );

                // Tambahkan tombol Back manual ke rootTable (karena logic defaultnya beda)
                TextButton backBtn = new TextButton("Back", ImageManager.skin);
                backBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        settingsWindow.remove();
                        rootTable.setVisible(true); // Munculkan menu lagi
                    }
                });
                settingsWindow.add(backBtn).padTop(20).row();

                stage.addActor(settingsWindow);
            }
        });

        // Aksi EXIT
        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() { stage.dispose(); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
